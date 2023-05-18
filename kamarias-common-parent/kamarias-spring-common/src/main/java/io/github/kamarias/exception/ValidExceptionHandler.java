package io.github.kamarias.exception;

import io.github.kamarias.dto.ResultDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用最高优先级处理
 * hibernate-validator 参数校验异常处理
 * @author wangyuxing@gogpay.cn
 * @date @DATE @TIME
 */
@ConditionalOnClass(Validated.class)
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidExceptionHandler {


    /**
     * 自定义类参数异常处理
     */
    @ExceptionHandler(BindException.class)
    public ResultDTO handleValidateException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>(8);
        bindingResult.getFieldErrors().forEach(item -> errorMap.put(item.getField(),item.getDefaultMessage()));
        return ResultDTO.error("参数校验异常",errorMap);
    }


    /**
     * jdk的包装类型 参数异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultDTO handleValidateException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>(8);
        bindingResult.getFieldErrors().forEach(item -> errorMap.put(item.getField(),item.getDefaultMessage()));
        return ResultDTO.error("参数校验异常",errorMap);
    }

    /**
     * 自定义的校验类型处理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResultDTO handleValidException(ConstraintViolationException e){
        // 获取异常信息
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        Map<Path, String> errorMap = constraintViolations.stream().collect(Collectors.toMap(ConstraintViolation::getPropertyPath, ConstraintViolation::getMessage));
        return ResultDTO.error("参数校验异常", errorMap);
    }

}
