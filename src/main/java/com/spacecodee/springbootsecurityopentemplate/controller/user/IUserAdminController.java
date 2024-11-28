package com.spacecodee.springbootsecurityopentemplate.controller.user;

import com.spacecodee.springbootsecurityopentemplate.data.pojo.ApiResponseDataPojo;
import com.spacecodee.springbootsecurityopentemplate.data.pojo.AuthenticationResponsePojo;
import com.spacecodee.springbootsecurityopentemplate.data.vo.user.AdminVO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface IUserAdminController {

    @PostMapping()
    ResponseEntity<ApiResponseDataPojo<AuthenticationResponsePojo>> add(@RequestBody @Valid AdminVO request, @RequestHeader(name = "Accept-Language", required = false, defaultValue = "en") String locale);

    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDataPojo<AuthenticationResponsePojo>> update(@RequestHeader(name = "Accept-Language", required = false, defaultValue = "en") String locale, @PathVariable int id, @RequestBody @Valid AdminVO request);

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDataPojo<AuthenticationResponsePojo>> delete(@RequestHeader(name = "Accept-Language", required = false, defaultValue = "en") String locale, @PathVariable int id);
}
