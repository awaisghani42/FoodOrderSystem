package model;

public class MenuItem {
    private int     itemId;
    private int     categoryId;
    private String  categoryName;
    private String  itemName;
    private String  description;
    private double  price;
    private String  imageUrl;
    private boolean isAvailable;

    // ── Constructors ──────────────────────────────────────────
    public MenuItem() {}

    public MenuItem(int categoryId, String itemName, String description,
                    double price, String imageUrl) {
        this.categoryId  = categoryId;
        this.itemName    = itemName;
        this.description = description;
        this.price       = price;
        this.imageUrl    = imageUrl;
        this.isAvailable = true;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public int     getItemId()       { return itemId;       }
    public int     getCategoryId()   { return categoryId;   }
    public String  getCategoryName() { return categoryName; }
    public String  getItemName()     { return itemName;     }
    public String  getDescription()  { return description;  }
    public double  getPrice()        { return price;        }
    public String  getImageUrl()     { return imageUrl;     }
    public boolean isAvailable()     { return isAvailable;  }

    public void setItemId(int itemId)              { this.itemId       = itemId;       }
    public void setCategoryId(int categoryId)      { this.categoryId   = categoryId;   }
    public void setCategoryName(String name)       { this.categoryName = name;         }
    public void setItemName(String itemName)       { this.itemName     = itemName;     }
    public void setDescription(String description) { this.description  = description;  }
    public void setPrice(double price)             { this.price        = price;        }
    public void setImageUrl(String imageUrl)       { this.imageUrl     = imageUrl;     }
    public void setAvailable(boolean available)    { this.isAvailable  = available;    }

    @Override
    public String toString() {
        return "MenuItem{id=" + itemId + ", name='" + itemName
                + "', price=" + price + ", available=" + isAvailable + "}";
    }
}
