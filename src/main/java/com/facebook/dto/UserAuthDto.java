package com.facebook.dto;

import com.facebook.enums.Provider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class UserAuthDto extends User {
    private long id;
    private Provider provider;

    public UserAuthDto() {
        super("default", "default", null);
    }

    public UserAuthDto(
            long id,
            String username,
            String password,
            Provider provider,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, password, authorities);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }
}
