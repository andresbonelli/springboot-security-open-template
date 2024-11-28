package com.spacecodee.springbootsecurityopentemplate.service.user;

import com.spacecodee.springbootsecurityopentemplate.data.dto.user.DeveloperDTO;
import com.spacecodee.springbootsecurityopentemplate.data.vo.user.DeveloperAVO;
import com.spacecodee.springbootsecurityopentemplate.data.vo.user.DeveloperUVO;

import java.util.List;

public interface IUserDeveloperService {
    void add(DeveloperAVO developerAVO, String locale);

    void update(int id, DeveloperUVO developerUVO, String locale);

    void delete(int id, String locale);

    DeveloperDTO findById(int id, String locale);

    List<DeveloperDTO> findAll(String locale);
}
