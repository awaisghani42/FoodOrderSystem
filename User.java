package model;

import java.time.LocalDateTime;

public class User {
    private int           userId;
    private String        name;
    private String        email;
    private String        password;   // stored as MD5 hash
    private String        phone;
    private String        role;       // "customer" or "admin"
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────
    public User() {}

    public User(String name, String email, String password, String phone) {
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.phone    = phone;
        this.role     = "customer";
    }

    // ── Getters & Setters ─────────────────────────────────────
    public int           getUserId()   { return userId;    }
    public String        getName()     { return name;      }
    public String        getEmail()    { return email;     }
    public String        getPassword() { return password;  }
    public String        getPhone()    { return phone;     }
    public String        getRole()     { return role;      }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    public void setUserId(int userId)            { this.userId    = userId;    }
    public void setName(String name)             { this.name      = name;      }
    public void setEmail(String email)           { this.email     = email;     }
    public void setPassword(String password)     { this.password  = password;  }
    public void setPhone(String phone)           { this.phone     = phone;     }
    public void setRole(String role)             { this.role      = role;      }
    public void setCreatedAt(LocalDateTime dt)   { this.createdAt = dt;        }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + name + "', role='" + role + "'}";
    }
}
