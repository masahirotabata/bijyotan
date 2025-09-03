// com.example.demo.service.CustomUserDetailsService.java
package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Trying login with: " + email);

        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        System.out.println("Found user: " + user.getEmail());

        // ===== ここで権限を付与 =====
        List<GrantedAuthority> authorities = new ArrayList<>();
        // ベースのロール
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // もしプレミアム等をロールに反映したい場合は、エンティティのgetter名に合わせて有効化してください。
        // try {
        //     // 例: boolean getPremium() / isPremium()
        //     boolean premium = false;
        //     try { premium = (Boolean) UserEntity.class.getMethod("getPremium").invoke(user); }
        //     catch (NoSuchMethodException ignore) {
        //         try { premium = (Boolean) UserEntity.class.getMethod("isPremium").invoke(user); }
        //         catch (NoSuchMethodException ignore2) {}
        //     }
        //     if (premium) {
        //         authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM"));
        //     }
        // } catch (Exception ignore) {}

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            authorities
        );
    }
}
