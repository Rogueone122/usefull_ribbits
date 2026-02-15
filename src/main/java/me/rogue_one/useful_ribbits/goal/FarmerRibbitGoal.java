package me.rogue_one.useful_ribbits.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import me.rogue_one.useful_ribbits.init.UsefulRibbitsModBlocks;

import java.util.*;

public class FarmerRibbitGoal extends Goal {
    private final Mob mob;
    private final double speed;
    private final int searchRange;
    
    // États du goal
    private enum FarmState {
        GOING_TO_CHEST,
        AT_CHEST_TAKING_SEEDS,
        PLANTING,
        GOING_TO_PLANT,
        HARVESTING,
        GOING_TO_HARVEST,
        DEPOSITING,
        WAITING_AT_CHEST
    }
    
    private FarmState currentState = FarmState.GOING_TO_CHEST;
    private BlockPos targetChest = null;
    private List<BlockPos> farmlands = new ArrayList<>();
    private List<BlockPos> plantableSpots = new ArrayList<>();
    private List<BlockPos> harvestableSpots = new ArrayList<>();
    private BlockPos currentTarget = null;
    private int tickCounter = 0;
    private int waitingTicks = 0;
    
    // Inventaire virtuel du mob
    private int seedCount = 0;
    private int wheatCount = 0;
    
    public FarmerRibbitGoal(Mob mob, double speed, int searchRange) {
        this.mob = mob;
        this.speed = speed;
        this.searchRange = searchRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }
    
    @Override
    public boolean canUse() {
        // Ne fonctionne que le jour
        if (!isDaytime()) {
            return false;
        }
        
        // Recherche un coffre si on n'en a pas
        if (targetChest == null) {
            targetChest = findNearestRibbitChestWithSeeds();
            if (targetChest != null) {
                currentState = FarmState.GOING_TO_CHEST;
                analyzeFarmArea();
                return true;
            }
        } else {
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean canContinueToUse() {
        return isDaytime() && targetChest != null;
    }
    
    @Override
    public void start() {
        tickCounter = 0;
        waitingTicks = 0;
        if (targetChest != null) {
            currentState = FarmState.GOING_TO_CHEST;
            analyzeFarmArea();
        }
    }
    
    @Override
    public void stop() {
        currentState = FarmState.GOING_TO_CHEST;
        targetChest = null;
        currentTarget = null;
        farmlands.clear();
        plantableSpots.clear();
        harvestableSpots.clear();
        mob.getNavigation().stop();
    }
    
    @Override
    public void tick() {
        if (!isDaytime()) {
            stop();
            return;
        }
        
        tickCounter++;
        
        switch (currentState) {
            case GOING_TO_CHEST:
                handleGoingToChest();
                break;
            case AT_CHEST_TAKING_SEEDS:
                handleTakingSeeds();
                break;
            case GOING_TO_PLANT:
                handleGoingToPlant();
                break;
            case PLANTING:
                handlePlanting();
                break;
            case GOING_TO_HARVEST:
                handleGoingToHarvest();
                break;
            case HARVESTING:
                handleHarvesting();
                break;
            case DEPOSITING:
                handleDepositing();
                break;
            case WAITING_AT_CHEST:
                handleWaitingAtChest();
                break;
        }
    }
    
    private void handleGoingToChest() {
        double distance = mob.distanceToSqr(targetChest.getX() + 0.5, targetChest.getY(), targetChest.getZ() + 0.5);
        
        if (distance < 1.5D) { // Très proche du coffre
            System.out.println("FarmerRibbitGoal - Arrivé au coffre!");
            mob.getNavigation().stop();
            currentState = FarmState.AT_CHEST_TAKING_SEEDS;
        } else {
            // Se déplacer vers le coffre (position exacte)
            mob.getNavigation().moveTo(targetChest.getX() + 0.5, targetChest.getY(), targetChest.getZ() + 0.5, speed);
        }
    }
    
    private void handleTakingSeeds() {
        // Analyser ce qui est nécessaire
        updateTaskLists();
        
        // Calculer combien de graines on a besoin
        int seedsNeeded = plantableSpots.size();
        int seedsToTake = Math.min(seedsNeeded, 64 - seedCount);
        
        if (seedsToTake > 0) {
            takeSeeds(seedsToTake);
        }
        
        // Décider de la prochaine action
        if (!harvestableSpots.isEmpty()) {
            // Il y a du blé à récolter
            currentTarget = harvestableSpots.get(0);
            currentState = FarmState.GOING_TO_HARVEST;
        } else if (!plantableSpots.isEmpty() && seedCount > 0) {
            // Il y a des spots à planter et on a des graines
            currentTarget = getClosestPlantableSpot();
            currentState = FarmState.GOING_TO_PLANT;
        } else {
            // Rien à faire, attendre au coffre
            currentState = FarmState.WAITING_AT_CHEST;
            waitingTicks = 0;
        }
    }
    
	private void handleGoingToPlant() {
	    if (currentTarget == null) {
	        currentState = FarmState.GOING_TO_CHEST;
	        return;
	    }
	    
	    // Position cible : un bloc à côté du bloc à planter pour permettre au mob d'arriver
	    double targetX = currentTarget.getX() + 0.5;
	    double targetY = currentTarget.getY() + 1; // Un bloc au-dessus du farmland
	    double targetZ = currentTarget.getZ() + 0.5;
	    
	    double distance = mob.distanceToSqr(targetX, targetY, targetZ);
	
	    System.out.println("FarmerRibbitGoal - Distance lors de la plantation: " + Math.sqrt(distance));
	    System.out.println("FarmerRibbitGoal - Position cible: " + targetX + ":" + targetY + ":" + targetZ);
	    System.out.println("FarmerRibbitGoal - Position mob: " + mob.getX() + ":" + mob.getY() + ":" + mob.getZ());
	    System.out.println("FarmerRibbitGoal - Navigation done: " + mob.getNavigation().isDone());
	    System.out.println("FarmerRibbitGoal - Navigation stuck: " + mob.getNavigation().isStuck());
	    
	    // Si on est proche (dans un rayon de 3 blocs), passer à la plantation
	    if (Math.sqrt(distance) < 1.5D) {
	        currentState = FarmState.PLANTING;
	        mob.getNavigation().stop();
	        System.out.println("FarmerRibbitGoal - Assez proche pour planter!");
	    } else {
	        // Continuer à naviguer vers la position
	        if (mob.getNavigation().isDone() || !mob.getNavigation().isInProgress()) {
	            System.out.println("FarmerRibbitGoal - Redémarrage de la navigation...");
	            boolean pathSet = mob.getNavigation().moveTo(targetX, targetY, targetZ, speed);
	            System.out.println("FarmerRibbitGoal - Chemin défini: " + pathSet);
	            
	            // Si impossible de définir un chemin après plusieurs tentatives, essayer une position alternative
	            if (!pathSet) {
	                System.out.println("FarmerRibbitGoal - Tentative avec position alternative...");
	                // Essayer différentes positions autour du bloc
	                for (int dx = -1; dx <= 1; dx++) {
	                    for (int dz = -1; dz <= 1; dz++) {
	                        if (dx == 0 && dz == 0) continue;
	                        double altX = currentTarget.getX() + 0.5 + dx;
	                        double altZ = currentTarget.getZ() + 0.5 + dz;
	                        if (mob.getNavigation().moveTo(altX, targetY, altZ, speed)) {
	                            System.out.println("FarmerRibbitGoal - Position alternative trouvée!");
	                            return;
	                        }
	                    }
	                }
	                // Si aucune position ne fonctionne, forcer la plantation
	                System.out.println("FarmerRibbitGoal - Aucun chemin possible, plantation forcée!");
	                currentState = FarmState.PLANTING;
	            }
	        }
	    }
	}
	
	private void handlePlanting() {
	    if (currentTarget == null || seedCount <= 0) {
	        currentState = FarmState.GOING_TO_CHEST;
	        return;
	    }
	    
	    // Vérifier si on peut planter (bloc au-dessus du farmland doit être vide)
	    BlockPos farmlandPos = currentTarget.below();
	    BlockState farmlandState = mob.level().getBlockState(farmlandPos);
	    BlockState currentBlockState = mob.level().getBlockState(currentTarget);
	    
	    System.out.println("FarmerRibbitGoal - Bloc farmland: " + farmlandState.getBlock());
	    System.out.println("FarmerRibbitGoal - Bloc cible: " + currentBlockState.getBlock());
	    System.out.println("FarmerRibbitGoal - Position mob pour plantation: " + mob.getX() + ":" + mob.getY() + ":" + mob.getZ());
	    System.out.println("FarmerRibbitGoal - Position cible: " + currentTarget.getX() + ":" + currentTarget.getY() + ":" + currentTarget.getZ());
	    
	    // Tentative de plantation
	    if (plantSeed(currentTarget)) {
	        seedCount--;
	        plantableSpots.remove(currentTarget);
	        System.out.println("FarmerRibbitGoal - Graine plantée avec succès! Graines restantes: " + seedCount);
	        
	        // Chercher le prochain spot le plus proche
	        if (!plantableSpots.isEmpty() && seedCount > 0) {
	            currentTarget = getClosestPlantableSpot();
	            currentState = FarmState.GOING_TO_PLANT;
	        } else {
	            // Plus de graines ou plus de spots, retourner au coffre
	            currentTarget = null;
	            currentState = FarmState.GOING_TO_CHEST;
	        }
	    } else {
	        System.out.println("FarmerRibbitGoal - ÉCHEC de plantation!");
	        System.out.println("FarmerRibbitGoal - Farmland valide: " + (farmlandState.getBlock() instanceof FarmBlock));
	        System.out.println("FarmerRibbitGoal - Bloc cible vide: " + currentBlockState.isAir());
	        
	        // Forcer la plantation si les conditions semblent bonnes
	        if (farmlandState.getBlock() instanceof FarmBlock && currentBlockState.isAir()) {
	            System.out.println("FarmerRibbitGoal - Tentative de plantation forcée...");
	            mob.level().setBlock(currentTarget, Blocks.WHEAT.defaultBlockState(), 3);
	            seedCount--;
	            plantableSpots.remove(currentTarget);
	            System.out.println("FarmerRibbitGoal - Plantation forcée réussie!");
	            
	            if (!plantableSpots.isEmpty() && seedCount > 0) {
	                currentTarget = getClosestPlantableSpot();
	                currentState = FarmState.GOING_TO_PLANT;
	            } else {
	                currentTarget = null;
	                currentState = FarmState.GOING_TO_CHEST;
	            }
	        } else {
	            // Échec définitif, retourner au coffre
	            currentState = FarmState.GOING_TO_CHEST;
	        }
	    }
	}
    
    private void handleGoingToHarvest() {
        if (currentTarget == null) {
            currentState = FarmState.GOING_TO_CHEST;
            return;
        }
        
        // Position exacte au-dessus du bloc à récolter - on vise le bloc même, pas au-dessus
        double targetX = currentTarget.getX() + 0.5;
        double targetY = currentTarget.getY(); // Légèrement plus bas pour être vraiment sur le bloc
        double targetZ = currentTarget.getZ() + 0.5;
        
        double distance = mob.distanceToSqr(targetX, targetY, targetZ);
        
        System.out.println("FarmerRibbitGoal - Distance à la récolte: " + Math.sqrt(distance) + " blocks");
        System.out.println(targetX + ":" + targetY + ":" + targetZ);
        
        if (distance < 1.5D) { // Extrêmement proche, vraiment sur le bloc
            currentState = FarmState.HARVESTING;
            mob.getNavigation().stop();
            System.out.println("FarmerRibbitGoal - Position atteinte, début récolte!");
        } else {
            // Forcer le mob à aller exactement sur le bloc
            mob.getNavigation().moveTo(targetX, targetY + 1, targetZ, speed);
            
            // Si on reste bloqué trop longtemps, forcer la téléportation
            if (mob.getNavigation().isStuck()) {
                mob.setPos(targetX, targetY, targetZ);
                currentState = FarmState.HARVESTING;
                System.out.println("FarmerRibbitGoal - Téléporté sur le bloc pour débloquer!");
            }
        }
    }
    
    private void handleHarvesting() {
        if (currentTarget == null) {
            currentState = FarmState.GOING_TO_CHEST;
            return;
        }
        
        // Vérifier qu'on est toujours bien positionné
        double targetX = currentTarget.getX() + 0.5;
        double targetY = currentTarget.getY();
        double targetZ = currentTarget.getZ() + 0.5;
        double distance = mob.distanceToSqr(targetX, targetY, targetZ);
        
        System.out.println("FarmerRibbitGoal - Distance lors de la récolte: " + Math.sqrt(distance));
        
        if (distance > 1.0D) {
            // Trop loin, se repositionner ou forcer la position
            System.out.println("FarmerRibbitGoal - Trop loin, repositionnement...");
            mob.setPos(targetX, targetY, targetZ); // Force la position
            return;
        }
        
        if (harvestCrop(currentTarget)) {
            harvestableSpots.remove(currentTarget);
            
            // Replanter immédiatement si on a des graines
            if (seedCount > 0) {
                if (plantSeed(currentTarget)) {
                    seedCount--;
                    System.out.println("FarmerRibbitGoal - Récolté et replanté!");
                }
            }
            
            // Retourner au coffre pour déposer
            currentTarget = null;
            currentState = FarmState.DEPOSITING;
        } else {
            // Échec de récolte, retourner au coffre
            System.out.println("FarmerRibbitGoal - Échec de récolte!");
            currentState = FarmState.GOING_TO_CHEST;
        }
    }
    
    private void handleDepositing() {
        double distance = mob.distanceToSqr(targetChest.getX() + 0.5, targetChest.getY(), targetChest.getZ() + 0.5);
        
        if (distance < 1.5D) {
            // Déposer les items
            depositItems();
            currentState = FarmState.WAITING_AT_CHEST;
            waitingTicks = 0;
        } else {
            // Retourner au coffre
            mob.getNavigation().moveTo(targetChest.getX() + 0.5, targetChest.getY(), targetChest.getZ() + 0.5, speed);
        }
    }
    
    private void handleWaitingAtChest() {
        waitingTicks++;
        
        // Vérifier toutes les 2 secondes s'il y a du travail
        if (waitingTicks >= 40) {
            waitingTicks = 0;
            updateTaskLists();
            
            if (!harvestableSpots.isEmpty()) {
                // Il y a du blé à récolter
                currentTarget = harvestableSpots.get(0);
                currentState = FarmState.GOING_TO_HARVEST;
            } else if (!plantableSpots.isEmpty() && chestHasSeeds(targetChest)) {
                // Il y a des spots à planter et le coffre a des graines
                currentState = FarmState.AT_CHEST_TAKING_SEEDS;
            }
        }
        
        // Rester près du coffre
        double distance = mob.distanceToSqr(targetChest.getX() + 0.5, targetChest.getY(), targetChest.getZ() + 0.5);
        if (distance > 2.0D) {
            mob.getNavigation().moveTo(targetChest.getX() + 0.5, targetChest.getY(), targetChest.getZ() + 0.5, speed);
        }
    }
    
    private BlockPos getClosestPlantableSpot() {
        if (plantableSpots.isEmpty()) return null;
        
        // Trier par distance du coffre (plus proche en premier)
        plantableSpots.sort((pos1, pos2) -> {
            double dist1 = targetChest.distSqr(pos1);
            double dist2 = targetChest.distSqr(pos2);
            return Double.compare(dist1, dist2);
        });
        
        return plantableSpots.get(0);
    }
    
    private boolean isDaytime() {
        Level level = mob.level();
        long time = level.getDayTime() % 24000;
        return time >= 0 && time < 13000;
    }
    
    private BlockPos findNearestRibbitChestWithSeeds() {
        BlockPos mobPos = mob.blockPosition();
        
        for (int x = -searchRange; x <= searchRange; x++) {
            for (int y = -8; y <= 8; y++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    BlockPos pos = mobPos.offset(x, y, z);
                    BlockState state = mob.level().getBlockState(pos);
                    
                    if (state.is(UsefulRibbitsModBlocks.RIBBIT_CHEST.get())) {
                        if (chestHasSeeds(pos)) {
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private boolean chestHasSeeds(BlockPos pos) {
        BlockEntity blockEntity = mob.level().getBlockEntity(pos);
        
        if (blockEntity instanceof ChestBlockEntity chest) {
            LazyOptional<IItemHandler> handlerOptional = chest.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (handlerOptional.isPresent()) {
                return handlerOptional.map(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (stack.is(Items.WHEAT_SEEDS)) {
                            return true;
                        }
                    }
                    return false;
                }).orElse(false);
            } else {
                try {
                    for (int i = 0; i < chest.getContainerSize(); i++) {
                        ItemStack stack = chest.getItem(i);
                        if (stack.is(Items.WHEAT_SEEDS)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    // Ignorer
                }
            }
        } else if (blockEntity != null) {
            LazyOptional<IItemHandler> genericHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
            return genericHandler.map(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.is(Items.WHEAT_SEEDS)) {
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        }
        
        return false;
    }
    
    private void analyzeFarmArea() {
        farmlands.clear();
        
        // Chercher les farmlands dans un rayon de 32 blocs du coffre
        for (int x = -32; x <= 32; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -32; z <= 32; z++) {
                    BlockPos pos = targetChest.offset(x, y, z);
                    BlockState state = mob.level().getBlockState(pos);
                    
                    if (state.getBlock() instanceof FarmBlock) {
                        farmlands.add(pos);
                    }
                }
            }
        }
        
        System.out.println("FarmerRibbitGoal - Trouvé " + farmlands.size() + " farmlands");
    }
    
    private void updateTaskLists() {
        plantableSpots.clear();
        harvestableSpots.clear();
        
        for (BlockPos farmPos : farmlands) {
            BlockPos cropPos = farmPos.above();
            BlockState cropState = mob.level().getBlockState(cropPos);
            
            if (cropState.isAir()) {
                plantableSpots.add(cropPos);
            } else if (cropState.getBlock() instanceof CropBlock cropBlock) {
                if (cropBlock.isMaxAge(cropState)) {
                    harvestableSpots.add(cropPos);
                }
            }
        }
        
        System.out.println("FarmerRibbitGoal - " + plantableSpots.size() + " spots plantables, " + harvestableSpots.size() + " à récolter");
    }
    
    private void takeSeeds(int amount) {
        BlockEntity blockEntity = mob.level().getBlockEntity(targetChest);
        
        if (blockEntity instanceof ChestBlockEntity chest) {
            LazyOptional<IItemHandler> handlerOptional = chest.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (handlerOptional.isPresent()) {
                handlerOptional.ifPresent(handler -> {
                    int taken = 0;
                    for (int i = 0; i < handler.getSlots() && taken < amount; i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (stack.is(Items.WHEAT_SEEDS)) {
                            int toTake = Math.min(stack.getCount(), amount - taken);
                            ItemStack extracted = handler.extractItem(i, toTake, false);
                            taken += extracted.getCount();
                            seedCount += extracted.getCount();
                        }
                    }
                    System.out.println("FarmerRibbitGoal - Pris " + taken + " graines. Total: " + seedCount);
                });
            } else {
                try {
                    int taken = 0;
                    for (int i = 0; i < chest.getContainerSize() && taken < amount; i++) {
                        ItemStack stack = chest.getItem(i);
                        if (stack.is(Items.WHEAT_SEEDS)) {
                            int toTake = Math.min(stack.getCount(), amount - taken);
                            ItemStack remaining = stack.copy();
                            remaining.shrink(toTake);
                            chest.setItem(i, remaining);
                            taken += toTake;
                            seedCount += toTake;
                        }
                    }
                    System.out.println("FarmerRibbitGoal - Pris " + taken + " graines (direct). Total: " + seedCount);
                } catch (Exception e) {
                    System.out.println("FarmerRibbitGoal - Erreur: " + e.getMessage());
                }
            }
        } else if (blockEntity != null) {
            LazyOptional<IItemHandler> genericHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
            genericHandler.ifPresent(handler -> {
                int taken = 0;
                for (int i = 0; i < handler.getSlots() && taken < amount; i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.is(Items.WHEAT_SEEDS)) {
                        int toTake = Math.min(stack.getCount(), amount - taken);
                        ItemStack extracted = handler.extractItem(i, toTake, false);
                        taken += extracted.getCount();
                        seedCount += extracted.getCount();
                    }
                }
                System.out.println("FarmerRibbitGoal - Pris " + taken + " graines (générique). Total: " + seedCount);
            });
        }
    }
    
    private boolean plantSeed(BlockPos pos) {
        BlockState farmland = mob.level().getBlockState(pos.below());
        if (farmland.getBlock() instanceof FarmBlock && mob.level().getBlockState(pos).isAir()) {
            mob.level().setBlock(pos, Blocks.WHEAT.defaultBlockState(), 3);
            return true;
        }
        return false;
    }
    
    private boolean harvestCrop(BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
            // Récolte améliorée : entre 1 et 3 blé, entre 0 et 3 graines
            int harvestedWheat = 1 + mob.level().random.nextInt(3); // 1 à 3 blé
            int harvestedSeeds = mob.level().random.nextInt(4);     // 0 à 3 graines
            
            wheatCount += harvestedWheat;
            seedCount += harvestedSeeds;
            
            // Limiter les graines à 64 max
            if (seedCount > 64) seedCount = 64;
            
            mob.level().destroyBlock(pos, false);
            System.out.println("FarmerRibbitGoal - Récolté " + harvestedWheat + " blé et " + harvestedSeeds + " graines! Total - Blé: " + wheatCount + ", Graines: " + seedCount);
            return true;
        }
        return false;
    }
    
    private void depositItems() {
        BlockEntity blockEntity = mob.level().getBlockEntity(targetChest);
        if (blockEntity instanceof ChestBlockEntity chest) {
            LazyOptional<IItemHandler> handlerOptional = chest.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (handlerOptional.isPresent()) {
                handlerOptional.ifPresent(handler -> {
                    // Déposer le blé
                    if (wheatCount > 0) {
                        ItemStack wheatStack = new ItemStack(Items.WHEAT, wheatCount);
                        for (int i = 0; i < handler.getSlots() && !wheatStack.isEmpty(); i++) {
                            wheatStack = handler.insertItem(i, wheatStack, false);
                        }
                        wheatCount = wheatStack.getCount();
                    }
                    
                    // Déposer les graines excédentaires
                    int seedsToDeposit = Math.max(0, seedCount - 16); // Garder 16 graines
                    if (seedsToDeposit > 0) {
                        ItemStack seedStack = new ItemStack(Items.WHEAT_SEEDS, seedsToDeposit);
                        for (int i = 0; i < handler.getSlots() && !seedStack.isEmpty(); i++) {
                            seedStack = handler.insertItem(i, seedStack, false);
                        }
                        seedCount -= (seedsToDeposit - seedStack.getCount());
                    }
                    System.out.println("FarmerRibbitGoal - Items déposés. Blé restant: " + wheatCount + ", Graines restantes: " + seedCount);
                });
            }
        } else if (blockEntity != null) {
            // Même logique pour les coffres customisés
            LazyOptional<IItemHandler> genericHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
            genericHandler.ifPresent(handler -> {
                if (wheatCount > 0) {
                    ItemStack wheatStack = new ItemStack(Items.WHEAT, wheatCount);
                    for (int i = 0; i < handler.getSlots() && !wheatStack.isEmpty(); i++) {
                        wheatStack = handler.insertItem(i, wheatStack, false);
                    }
                    wheatCount = wheatStack.getCount();
                }
                
                int seedsToDeposit = Math.max(0, seedCount - 16);
                if (seedsToDeposit > 0) {
                    ItemStack seedStack = new ItemStack(Items.WHEAT_SEEDS, seedsToDeposit);
                    for (int i = 0; i < handler.getSlots() && !seedStack.isEmpty(); i++) {
                        seedStack = handler.insertItem(i, seedStack, false);
                    }
                    seedCount -= (seedsToDeposit - seedStack.getCount());
                }
            });
        }
    }
}