package me.rogue_one.useful_ribbits.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.Container;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import me.rogue_one.useful_ribbits.init.UsefulRibbitsModBlocks;

import java.util.*;

public class RibbitTransferMeatGoal extends Goal {

    private final Mob mob;
    private final double speed;
    private final int searchRange;

    private BlockPos smokerPos;
    private BlockPos chestPos;
    private TaskState currentTask = TaskState.SEARCHING;
    private Item currentMeatType;
    private int carriedCookedAmount = 0;
    private int carriedRawAmount = 0;
    private int neededRawAmount = 0;
    private int tickCounter = 0;
    private int checkTimer = 0; // Timer pour vérifier les conditions périodiquement
    private boolean hasPlayedOpenSound = false;

    // Liste des viandes et poissons crus
    private static final Set<Item> RAW_FOODS = Set.of(
            Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.RABBIT, Items.MUTTON,
            Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH
    );

    // Mapping viande crue -> viande cuite
    private static final Map<Item, Item> COOKING_MAP = Map.of(
            Items.BEEF, Items.COOKED_BEEF,
            Items.PORKCHOP, Items.COOKED_PORKCHOP,
            Items.CHICKEN, Items.COOKED_CHICKEN,
            Items.RABBIT, Items.COOKED_RABBIT,
            Items.MUTTON, Items.COOKED_MUTTON,
            Items.COD, Items.COOKED_COD,
            Items.SALMON, Items.COOKED_SALMON
    );

    private enum TaskState {
        SEARCHING,           // Cherche le fumoir
        GOING_TO_SMOKER,     // Va vers le fumoir
        AT_SMOKER_WAITING,   // Attend au fumoir et surveille
        GOING_TO_CHEST,      // Va vers le coffre
        AT_CHEST_WORKING,    // Travaille au coffre (dépose cuit, prend cru)
        RETURNING_TO_SMOKER, // Retourne au fumoir avec viande crue
        RETURNING_EXCESS,    // Retourne l'excès au coffre
        WORKING_AT_SMOKER    // Travaille au fumoir (met viande, attend cuisson)
    }

    public RibbitTransferMeatGoal(Mob mob, double speed, int searchRange) {
        this.mob = mob;
        this.speed = speed;
        this.searchRange = searchRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Ne fonctionne que de jour
        if (!isDaytime()) {
            return false;
        }

        // Recherche le fumoir si nécessaire
        if (currentTask == TaskState.SEARCHING) {
            findSmoker();
        }

        return smokerPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return isDaytime() && smokerPos != null;
    }

    @Override
    public void start() {
        currentTask = TaskState.GOING_TO_SMOKER;
        resetVariables();
    }

    @Override
    public void stop() {
        currentTask = TaskState.SEARCHING;
        mob.getNavigation().stop();
        resetVariables();
    }

    @Override
    public void tick() {
        tickCounter++;
        checkTimer++;

        switch (currentTask) {
            case GOING_TO_SMOKER:
                handleGoingToSmoker();
                break;
            case AT_SMOKER_WAITING:
                handleAtSmokerWaiting();
                break;
            case GOING_TO_CHEST:
                handleGoingToChest();
                break;
            case AT_CHEST_WORKING:
                handleAtChestWorking();
                break;
            case RETURNING_TO_SMOKER:
                handleReturningToSmoker();
                break;
            case RETURNING_EXCESS:
                handleReturningExcess();
                break;
            case WORKING_AT_SMOKER:
                handleWorkingAtSmoker();
                break;
        }
    }

    private void resetVariables() {
        currentMeatType = null;
        carriedCookedAmount = 0;
        carriedRawAmount = 0;
        neededRawAmount = 0;
        checkTimer = 0;
        hasPlayedOpenSound = false;
        chestPos = null;
    }

    private boolean isDaytime() {
        Level level = mob.level();
        long timeOfDay = level.getDayTime() % 24000;
        return timeOfDay >= 0 && timeOfDay < 13000;
    }

    private void findSmoker() {
        BlockPos mobPos = mob.blockPosition();
        smokerPos = null;

        // Recherche le fumoir dans un rayon autour de l'entité
        for (int x = -searchRange; x <= searchRange; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    BlockPos pos = mobPos.offset(x, y, z);
                    BlockState state = mob.level().getBlockState(pos);

                    if (state.is(Blocks.SMOKER)) {
                        smokerPos = pos;
                        return;
                    }
                }
            }
        }
    }

    private BlockPos findNearestChestWithRawFood(BlockPos center) {
        // Recherche le coffre le plus proche avec de la viande crue
        for (int x = -searchRange; x <= searchRange; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = mob.level().getBlockState(pos);

                    if (state.is(UsefulRibbitsModBlocks.RIBBIT_CHEST.get())) {
                        BlockEntity blockEntity = mob.level().getBlockEntity(pos);
                        if (blockEntity instanceof Container container) {
                            if (hasRawFoodInContainer(container)) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findNearestChest(BlockPos center) {
        // Recherche le coffre le plus proche (pour déposer viande cuite)
        for (int x = -searchRange; x <= searchRange; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = mob.level().getBlockState(pos);

                    if (state.is(UsefulRibbitsModBlocks.RIBBIT_CHEST.get())) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasRawFoodInContainer(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && RAW_FOODS.contains(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    private void handleGoingToSmoker() {
        if (mob.distanceToSqr(smokerPos.getX(), smokerPos.getY(), smokerPos.getZ()) < 2.5) {
            currentTask = TaskState.AT_SMOKER_WAITING;
        } else {
            navigateTo(smokerPos);
        }
    }

    private void handleAtSmokerWaiting() {
        // Vérifier périodiquement (toutes les 20 ticks = 1 seconde)
        if (checkTimer % 20 != 0) return;

        BlockEntity blockEntity = mob.level().getBlockEntity(smokerPos);
        if (blockEntity instanceof SmokerBlockEntity smokerEntity) {
            ItemStack output = smokerEntity.getItem(2);

            // Priorité 1: S'il y a de la viande cuite, la récupérer
            if (!output.isEmpty()) {
                carriedCookedAmount = output.getCount();
                currentMeatType = getCookedMeatRawType(output.getItem());
                smokerEntity.setItem(2, ItemStack.EMPTY);

                // Chercher un coffre pour déposer
                chestPos = findNearestChest(smokerPos);
                if (chestPos != null) {
                    currentTask = TaskState.GOING_TO_CHEST;
                }
                return;
            }

            // Priorité 2: Chercher de la viande crue à traiter
            chestPos = findNearestChestWithRawFood(smokerPos);
            if (chestPos != null) {
                // Calculer ce dont on a besoin
                calculateNeeds(smokerEntity);
                if (neededRawAmount > 0 || carriedCookedAmount > 0) {
                    currentTask = TaskState.GOING_TO_CHEST;
                }
            }
        }
    }

    private Item getCookedMeatRawType(Item cookedItem) {
        for (Map.Entry<Item, Item> entry : COOKING_MAP.entrySet()) {
            if (entry.getValue() == cookedItem) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void calculateNeeds(SmokerBlockEntity smokerEntity) {
        ItemStack currentInput = smokerEntity.getItem(0);

        // Trouver le type de viande à traiter
        if (currentMeatType == null && chestPos != null) {
            BlockEntity blockEntity = mob.level().getBlockEntity(chestPos);
            if (blockEntity instanceof Container container) {
                currentMeatType = findFirstRawFoodType(container);
            }
        }

        if (currentMeatType == null) {
            neededRawAmount = 0;
            return;
        }

        // Si le fumoir est vide ou contient le même type
        if (currentInput.isEmpty()) {
            neededRawAmount = 64;
        } else if (currentInput.getItem() == currentMeatType) {
            neededRawAmount = 64 - currentInput.getCount();
        } else {
            neededRawAmount = 0; // Type différent, on ne peut rien ajouter
        }
    }

    private Item findFirstRawFoodType(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && RAW_FOODS.contains(stack.getItem())) {
                return stack.getItem();
            }
        }
        return null;
    }

    private void handleGoingToChest() {
        if (mob.distanceToSqr(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5) < 1.5) {
            currentTask = TaskState.AT_CHEST_WORKING;

            // Jouer le son d'ouverture du coffre
            if (!hasPlayedOpenSound) {
                mob.level().playSound(null, chestPos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f,
                        mob.level().random.nextFloat() * 0.1f + 0.9f);
                hasPlayedOpenSound = true;
            }
        } else {
            navigateTo(chestPos);
        }
    }

    private void handleAtChestWorking() {
        BlockEntity blockEntity = mob.level().getBlockEntity(chestPos);
        if (blockEntity instanceof Container container) {

            // Étape 1: Déposer la viande cuite si on en a
            if (carriedCookedAmount > 0) {
                Item cookedItem = COOKING_MAP.get(currentMeatType);
                if (cookedItem != null) {
                    ItemStack toStore = new ItemStack(cookedItem, carriedCookedAmount);
                    storeItemInContainer(container, toStore);
                    carriedCookedAmount = 0;
                }
            }

            // Étape 2: Prendre la viande crue nécessaire
            if (neededRawAmount > 0) {
                // Vérifier si la viande existe encore
                if (getTotalItemsInContainer(container, currentMeatType) == 0) {
                    // Plus de cette viande, chercher un autre type
                    currentMeatType = findFirstRawFoodType(container);
                    if (currentMeatType != null) {
                        // Recalculer les besoins avec le nouveau type
                        BlockEntity smokerBlockEntity = mob.level().getBlockEntity(smokerPos);
                        if (smokerBlockEntity instanceof SmokerBlockEntity smokerEntity) {
                            calculateNeeds(smokerEntity);
                        }
                    } else {
                        neededRawAmount = 0;
                    }
                }

                if (neededRawAmount > 0 && currentMeatType != null) {
                    int availableInChest = getTotalItemsInContainer(container, currentMeatType);
                    int toTake = Math.min(neededRawAmount, availableInChest);
                    carriedRawAmount = extractItemsFromContainer(container, currentMeatType, toTake);
                }
            }

            // Jouer le son de fermeture du coffre
            mob.level().playSound(null, chestPos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f,
                    mob.level().random.nextFloat() * 0.1f + 0.9f);
            hasPlayedOpenSound = false;

            // Retourner au fumoir
            if (carriedRawAmount > 0) {
                currentTask = TaskState.RETURNING_TO_SMOKER;
            } else {
                currentTask = TaskState.AT_SMOKER_WAITING;
                navigateTo(smokerPos);
            }
        }
    }

    private void handleReturningToSmoker() {
        // Vérifier si les besoins ont changé pendant le trajet
        BlockEntity blockEntity = mob.level().getBlockEntity(smokerPos);
        if (blockEntity instanceof SmokerBlockEntity smokerEntity) {
            ItemStack currentInput = smokerEntity.getItem(0);
            int currentNeeded;

            if (currentInput.isEmpty()) {
                currentNeeded = 64;
            } else if (currentInput.getItem() == currentMeatType) {
                currentNeeded = 64 - currentInput.getCount();
            } else {
                currentNeeded = 0;
            }

            // Si on porte plus que nécessaire, retourner l'excès
            if (carriedRawAmount > currentNeeded) {
                currentTask = TaskState.RETURNING_EXCESS;
                return;
            }
        }

        // Continuer vers le fumoir
        if (mob.distanceToSqr(smokerPos.getX(), smokerPos.getY(), smokerPos.getZ()) < 2.5) {
            currentTask = TaskState.WORKING_AT_SMOKER;
        } else {
            navigateTo(smokerPos);
        }
    }

    private void handleReturningExcess() {
        if (mob.distanceToSqr(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5) < 1.5) {
            BlockEntity blockEntity = mob.level().getBlockEntity(chestPos);
            if (blockEntity instanceof Container container) {
                // Calculer l'excès à retourner
                BlockEntity smokerBlockEntity = mob.level().getBlockEntity(smokerPos);
                if (smokerBlockEntity instanceof SmokerBlockEntity smokerEntity) {
                    ItemStack currentInput = smokerEntity.getItem(0);
                    int currentNeeded;

                    if (currentInput.isEmpty()) {
                        currentNeeded = 64;
                    } else if (currentInput.getItem() == currentMeatType) {
                        currentNeeded = 64 - currentInput.getCount();
                    } else {
                        currentNeeded = 0;
                    }

                    int toReturn = carriedRawAmount - currentNeeded;
                    if (toReturn > 0) {
                        ItemStack excessStack = new ItemStack(currentMeatType, toReturn);
                        storeItemInContainer(container, excessStack);
                        carriedRawAmount -= toReturn;
                    }
                }
            }

            // Retourner au fumoir avec la bonne quantité
            currentTask = TaskState.RETURNING_TO_SMOKER;
        } else {
            navigateTo(chestPos);
        }
    }

    private void handleWorkingAtSmoker() {
        BlockEntity blockEntity = mob.level().getBlockEntity(smokerPos);
        if (blockEntity instanceof SmokerBlockEntity smokerEntity) {

            // Mettre la viande crue dans le fumoir
            if (carriedRawAmount > 0) {
                ItemStack currentInput = smokerEntity.getItem(0);

                if (currentInput.isEmpty()) {
                    smokerEntity.setItem(0, new ItemStack(currentMeatType, carriedRawAmount));
                    carriedRawAmount = 0;
                } else if (currentInput.getItem() == currentMeatType) {
                    int canAdd = 64 - currentInput.getCount();
                    int toAdd = Math.min(carriedRawAmount, canAdd);
                    currentInput.grow(toAdd);
                    smokerEntity.setItem(0, currentInput);
                    carriedRawAmount -= toAdd;
                }
            }

            // Attendre et surveiller
            ItemStack output = smokerEntity.getItem(2);
            ItemStack input = smokerEntity.getItem(0);

            // Si la viande cuite atteint 64 ou plus d'input, aller déposer
            if (output.getCount() >= 64 || (input.isEmpty() && output.getCount() > 0)) {
                carriedCookedAmount = output.getCount();
                smokerEntity.setItem(2, ItemStack.EMPTY);

                chestPos = findNearestChest(smokerPos);
                if (chestPos != null) {
                    currentTask = TaskState.GOING_TO_CHEST;
                    neededRawAmount = 0; // Reset pour ne pas reprendre de viande crue
                } else {
                    currentTask = TaskState.AT_SMOKER_WAITING;
                }
            } else if (input.isEmpty() && output.isEmpty()) {
                // Plus rien dans le fumoir, retourner en mode attente
                currentTask = TaskState.AT_SMOKER_WAITING;
                currentMeatType = null;
            }
        }
    }

    private void navigateTo(BlockPos pos) {
        PathNavigation navigation = mob.getNavigation();
        navigation.moveTo(pos.getX(), pos.getY(), pos.getZ(), speed);
    }

    private int getTotalItemsInContainer(Container container, Item item) {
        int total = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() == item) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private int extractItemsFromContainer(Container container, Item item, int amount) {
        int extracted = 0;
        for (int i = 0; i < container.getContainerSize() && extracted < amount; i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() == item) {
                int toExtract = Math.min(amount - extracted, stack.getCount());
                stack.shrink(toExtract);
                container.setItem(i, stack);
                extracted += toExtract;
            }
        }
        return extracted;
    }

    private void storeItemInContainer(Container container, ItemStack itemStack) {
        // Essaie d'abord de combiner avec les stacks existants
        for (int i = 0; i < container.getContainerSize() && !itemStack.isEmpty(); i++) {
            ItemStack existingStack = container.getItem(i);
            if (!existingStack.isEmpty() && existingStack.getItem() == itemStack.getItem()) {
                int canAdd = existingStack.getMaxStackSize() - existingStack.getCount();
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, itemStack.getCount());
                    existingStack.grow(toAdd);
                    itemStack.shrink(toAdd);
                    container.setItem(i, existingStack);
                }
            }
        }

        // Place dans les slots vides
        for (int i = 0; i < container.getContainerSize() && !itemStack.isEmpty(); i++) {
            if (container.getItem(i).isEmpty()) {
                container.setItem(i, itemStack.copy());
                itemStack.setCount(0);
                break;
            }
        }
    }
}