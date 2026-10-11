package ch.ismoo.obamcraft.wtf;

import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/** Default-off foundation, not a substitute for Trust, claims or the energy economy. */
public final class WtfFoundation extends JavaPlugin implements Listener, CommandExecutor {
  private NamespacedKey decoration, prepared;
  private BukkitTask buildTask, orbitTask;
  private final List<UUID> allays=new ArrayList<>();
  private int floor;
  private boolean enabled;
  @Override public void onEnable() {
    saveDefaultConfig();enabled=getConfig().getBoolean("enabled",false);
    floor=getConfig().getInt("floor-y",100);
    decoration=new NamespacedKey(this,"decorative_allay");prepared=new NamespacedKey(this,"prepared");
    Objects.requireNonNull(getCommand("wtf")).setExecutor(this);
    if(!enabled){getLogger().info("WTF foundation disabled; no world, protection or entity changes.");return;}
    getServer().getPluginManager().registerEvents(this,this);
    World w=world();
    if(w!=null)for(Entity e:w.getEntities())if(decorative(e))allays.add(e.getUniqueId());
    orbitTask=getServer().getScheduler().runTaskTimer(this,()->orbit(),1,5);
    getLogger().info("Foundation only: protected spawn/roads and decorative stabilizer. Energy, claims and Trust unavailable.");
  }
  @Override public void onDisable(){if(buildTask!=null)buildTask.cancel();if(orbitTask!=null)orbitTask.cancel();}
  private World world(){return Bukkit.getWorld(getConfig().getString("world","wtf_beta"));}
  private boolean target(World w){return enabled&&w!=null&&w==world();}
  private boolean protect(Block b){return target(b.getWorld())&&SpawnPolicy.protectedBlock(b.getX(),b.getZ());}
  private boolean decorative(Entity e){return e.getPersistentDataContainer().has(decoration,PersistentDataType.BYTE);}
  @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
    if(!enabled){sender.sendMessage("WTF is not open: its beta foundation is disabled.");return true;}
    if(args.length==0||args[0].equalsIgnoreCase("status")){
      sender.sendMessage("WTF foundation: spawn/road protection and decorative stabilizer only. Energy, claims, Trust and gameplay are not ready.");return true;
    }
    if(!Arrays.equals(Arrays.copyOf(args,Math.min(2,args.length)),new String[]{"beta","prepare"})){
      sender.sendMessage("Use /wtf status. The full beta commands are not available.");return true;
    }
    if(!(sender instanceof ConsoleCommandSender)){sender.sendMessage("Disposable beta preparation is console-only.");return true;}
    World w=world();
    if(w==null||!w.getName().equals("wtf_beta")||!getConfig().getBoolean("allow-disposable-bootstrap",false)){
      sender.sendMessage("Preparation refused: require a verified disposable wtf_beta world and explicit bootstrap setting.");return true;
    }
    if(args.length!=4||!args[2].equals("confirm")||!args[3].equals(w.getUID().toString())){
      sender.sendMessage("This changes only the 80x80 beta spawn layers and decoration. Confirm exact world UUID with: wtf beta prepare confirm "+w.getUID());return true;
    }
    if(floor<w.getMinHeight()+1||floor+12>=w.getMaxHeight()||buildTask!=null||w.getPersistentDataContainer().has(prepared,PersistentDataType.BYTE)){
      sender.sendMessage("Preparation refused: invalid height, already running or already prepared. No automatic rebuild.");return true;
    }
    final int[] cursor={0};final int total=80*80*12;
    buildTask=getServer().getScheduler().runTaskTimer(this,()->{
      long deadline=System.nanoTime()+3_000_000L;int wrote=0;
      while(cursor[0]<total&&wrote<256&&System.nanoTime()<deadline){
        int n=cursor[0]++,layer=n/(80*80),cell=n%(80*80),x=-40+cell%80,z=-40+cell/80;
        Material material=layer==0?(x==-40||x==39||z==-40||z==39?Material.YELLOW_CONCRETE:Material.GRASS_BLOCK):Material.AIR;
        w.getBlockAt(x,floor+layer,z).setType(material,false);wrote++;
      }
      if(cursor[0]>=total){
        buildTask.cancel();buildTask=null;finishDecoration(w);
        w.getPersistentDataContainer().set(prepared,PersistentDataType.BYTE,(byte)1);
        w.setSpawnLocation(8,floor+1,8);w.save();sender.sendMessage("Disposable spawn prepared. This is still a foundation, not the full beta.");
      }
    },1,1);
    sender.sendMessage("Preparing disposable spawn with paced block writes. No other world is touched.");return true;
  }
  private void finishDecoration(World w){
    for(int x=-4;x<=4;x++)for(int y=-4;y<=4;y++)for(int z=-4;z<=4;z++){
      int d=x*x+y*y+z*z;if(d>=12&&d<=20)w.getBlockAt(x,floor+5+y,z).setType(Material.GLASS,false);
    }
    w.getBlockAt(0,floor+5,0).setType(Material.GLOWSTONE,false);
    for(int i=0;i<8;i++){
      double a=i*Math.PI/4;
      Allay e=w.spawn(new Location(w,Math.cos(a)*2+.5,floor+5+Math.sin(a*2)*.6,Math.sin(a)*2+.5),Allay.class,m->{
        m.getPersistentDataContainer().set(decoration,PersistentDataType.BYTE,(byte)1);
        m.setAI(false);m.setInvulnerable(true);m.setCanPickupItems(false);m.setPersistent(true);
      });allays.add(e.getUniqueId());
    }
  }
  private void orbit(){
    World w=world();if(w==null)return;
    double time=Bukkit.getCurrentTick()*.025;
    for(int i=0;i<allays.size();i++){
      Entity e=Bukkit.getEntity(allays.get(i));if(e==null||!e.isValid()||e.getWorld()!=w)continue;
      double a=time+i*Math.PI/4;
      e.teleport(new Location(w,.5+Math.cos(a)*2,floor+5+Math.sin(a*2)*.6,.5+Math.sin(a)*2));
    }
  }
  @EventHandler(ignoreCancelled=true) public void broken(BlockBreakEvent e){if(protect(e.getBlock()))e.setCancelled(true);}
  @EventHandler(ignoreCancelled=true) public void placed(BlockPlaceEvent e){if(protect(e.getBlock()))e.setCancelled(true);}
  @EventHandler(ignoreCancelled=true) public void bucketEmpty(PlayerBucketEmptyEvent e){if(protect(e.getBlock())||protect(e.getBlockClicked()))e.setCancelled(true);}
  @EventHandler(ignoreCancelled=true) public void bucketFill(PlayerBucketFillEvent e){if(protect(e.getBlock())||protect(e.getBlockClicked()))e.setCancelled(true);}
  @EventHandler(ignoreCancelled=true) public void entityBlock(EntityChangeBlockEvent e){if(protect(e.getBlock()))e.setCancelled(true);}
  @EventHandler(ignoreCancelled=true) public void flow(BlockFromToEvent e){if(protect(e.getToBlock()))e.setCancelled(true);}
  @EventHandler(ignoreCancelled=true) public void piston(BlockPistonExtendEvent e){if(e.getBlocks().stream().anyMatch(b->protect(b)||protect(b.getRelative(e.getDirection()))))e.setCancelled(true);}
  @EventHandler(ignoreCancelled=true) public void retract(BlockPistonRetractEvent e){if(e.getBlocks().stream().anyMatch(b->protect(b)||protect(b.getRelative(e.getDirection()))))e.setCancelled(true);}
  @EventHandler(ignoreCancelled=true) public void explode(EntityExplodeEvent e){e.blockList().removeIf(this::protect);}
  @EventHandler(ignoreCancelled=true) public void blockExplode(BlockExplodeEvent e){e.blockList().removeIf(this::protect);}
  @EventHandler(ignoreCancelled=true) public void mob(CreatureSpawnEvent e){
    Location p=e.getLocation();if(target(p.getWorld())&&SpawnPolicy.protectedBlock(p.getBlockX(),p.getBlockZ())&&!decorative(e.getEntity()))e.setCancelled(true);
  }
  @EventHandler(ignoreCancelled=true) public void damage(EntityDamageEvent e){
    Location p=e.getEntity().getLocation();if(decorative(e.getEntity())||target(p.getWorld())&&SpawnPolicy.spawn(p.getBlockX(),p.getBlockZ()))e.setCancelled(true);
  }
  @EventHandler(ignoreCancelled=true) public void interact(PlayerInteractEntityEvent e){if(decorative(e.getRightClicked()))e.setCancelled(true);}
  @EventHandler(ignoreCancelled=true) public void leash(PlayerLeashEntityEvent e){if(decorative(e.getEntity()))e.setCancelled(true);}
}
