package cheezbags;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;

public class PredicateMob {
    
    public PredicateMob() {}

    private Set<String> names, meta;
    private Set<String> notnames, notmeta;
    
    public void setNames(String names) {
        this.names = new HashSet<>();
        for (String name : names.split(",")) {
            if (!name.isEmpty()) {
                if (name.startsWith("!")) {
                    this.notnames.add(name);
                    continue;
                }
                this.names.add(name);
            }
        }
    }
    
    public void setMeta(String metas) {
        this.meta = new HashSet<>();
        for (String m : metas.split(",")) {
            if (!m.isEmpty()) {
                if (m.startsWith("!")) {
                    this.notmeta.add(m);
                    continue;
                }
                this.meta.add(m);
            }
        }
    }
    
    public boolean matches(Entity e) {
        String s = e.getCustomName();
        if (s != null) {
            if (this.names.contains(s)) {
                return true;
            }
            if (this.notnames.contains(s)) {
                return false;
            }
        }
        
        for (String m : this.meta) {
            if (e.hasMetadata(m)) {
                return true;
            }
        }
        
        for (String m : this.notmeta) {
            if (e.hasMetadata(m)) {
                return false;
            }
        }
        return false;
    }
    
    public boolean isEmpty() {
        return this.names.isEmpty() && this.meta.isEmpty() && this.notnames.isEmpty() && this.notmeta.isEmpty();
    }

}
