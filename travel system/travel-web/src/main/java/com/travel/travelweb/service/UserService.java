package com.travel.travelweb.service;

import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.LandCollectRepository;
import com.travel.travelweb.repo.OrdinaryUserRepository;
import com.travel.travelweb.repo.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    public static final String USER_TYPE_ADMIN = "1";
    public static final String USER_TYPE_ORDINARY = "2";

    private final SysUserRepository userRepository;
    private final LandCollectRepository landCollectRepository;
    private final OrdinaryUserRepository ordinaryUserRepository;

    public UserService(SysUserRepository userRepository, 
                      LandCollectRepository landCollectRepository,
                      OrdinaryUserRepository ordinaryUserRepository) {
        this.userRepository = userRepository;
        this.landCollectRepository = landCollectRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
    }

    public List<SysUser> findOrdinaryUsers(String keyword) {
        return userRepository.searchUsers(USER_TYPE_ORDINARY, keyword);
    }

    public Optional<SysUser> findById(String userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public boolean deleteUser(String userId) {
        if (userRepository.existsById(userId)) {
            landCollectRepository.deleteByUserId(userId);
            ordinaryUserRepository.deleteByUserId(userId);
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateUserName(String userId, String newUserName) {
        return userRepository.findById(userId).map(user -> {
            user.setUserName(newUserName);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }
}
