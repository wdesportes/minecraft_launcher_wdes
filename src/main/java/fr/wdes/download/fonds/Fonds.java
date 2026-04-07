package fr.wdes.download.fonds;

import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.Map;

public class Fonds
{

  protected Map<String, Fond> fonds;

  
  public Fonds()
  {
    this.fonds = new LinkedHashMap<String, Fond>();
  }
  
  public Map<String, Fond> getFileMap()
  {
    return this.fonds;
  }
  
  public Map<Fond, String> getUniqueObjects()
  {
    Map<Fond, String> result = Maps.newHashMap();
    for (Map.Entry<String, Fond> objectEntry : this.fonds.entrySet()) {
      result.put(objectEntry.getValue(), objectEntry.getKey());
    }
    return result;
  }
  

  
  public class Fond
  {
	  
	  protected String MD5;
	  
	  protected String name;
	  
	  protected long size;
	  
	  protected String hash;
	  
	  protected boolean reconstruct;
	  
	  protected String compressedHash;
	  
	  protected long compressedSize;
    public Fond() {}
    
    public String getName()
    {
      return this.name;
    }
    
    public String getMD5()
    {
      return this.MD5;
    }

    
    public void AssetObject() {}
    
    public String getHash()
    {
      return this.hash;
    }
    
    public long getSize()
    {
      return this.size;
    }
    
    public boolean shouldReconstruct()
    {
      return this.reconstruct;
    }
    
    public boolean hasCompressedAlternative()
    {
      return this.compressedHash != null;
    }
    
    public String getCompressedHash()
    {
      return this.compressedHash;
    }
    
    public long getCompressedSize()
    {
      return this.compressedSize;
    }
    
    public boolean equals(Object o)
    {
      if (this == o) {
        return true;
      }
      if ((o == null) || (getClass() != o.getClass())) {
        return false;
      }
      Fond that = (Fond)o;
      if (this.compressedSize != that.compressedSize) {
        return false;
      }
      if (this.reconstruct != that.reconstruct) {
        return false;
      }
      if (this.size != that.size) {
        return false;
      }
      if (this.compressedHash != null ? !this.compressedHash.equals(that.compressedHash) : that.compressedHash != null) {
        return false;
      }
      if (this.hash != null ? !this.hash.equals(that.hash) : that.hash != null) {
        return false;
      }
      return true;
    }
    
    public int hashCode()
    {
      int result = this.hash != null ? this.hash.hashCode() : 0;
      result = 31 * result + (int)(this.size ^ this.size >>> 32);
      result = 31 * result + (this.reconstruct ? 1 : 0);
      result = 31 * result + (this.compressedHash != null ? this.compressedHash.hashCode() : 0);
      result = 31 * result + (int)(this.compressedSize ^ this.compressedSize >>> 32);
      return result;
    }


    

  }
}
