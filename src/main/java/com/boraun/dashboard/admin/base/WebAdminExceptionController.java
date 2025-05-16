package com.boraun.dashboard.admin.base;

import com.boraun.dashboard.common.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.NoSuchElementException;

@Slf4j
public class WebAdminExceptionController {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handlingNoSuchElementException(NoSuchElementException e, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String url = httpServletRequest.getRequestURL().toString();
        String queryString = httpServletRequest.getQueryString();
        String requestUrl = url + (Utils.nonNull(queryString) ? "?" + queryString : "");
        log.error("Error in handlingNoSuchElementException accessing " + requestUrl + " with message " + e.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("web/admin/error");
        modelAndView.addObject("message", e.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handlingNullPointerException(NullPointerException e, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String url = httpServletRequest.getRequestURL().toString();
        String queryString = httpServletRequest.getQueryString();
        String requestUrl = url + (Utils.nonNull(queryString) ? "?" + queryString : "");
        log.error("Error in handlingNullPointerException accessing " + requestUrl + " with message " + e.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("web/admin/error");
        modelAndView.addObject("message", e.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(WebAdminException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public ModelAndView handlingSuccessException(WebAdminException e) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("fragments/message");
        modelAndView.addObject("message", e.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(WebAdminErrorException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public ModelAndView handlingErrorException(WebAdminErrorException e) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("fragments/message");
        modelAndView.addObject("message", e.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public ModelAndView handlingDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String url = httpServletRequest.getRequestURL().toString();
        String queryString = httpServletRequest.getQueryString();
        String requestUrl = url + (Utils.nonNull(queryString) ? "?" + queryString : "");
        log.error("Error in handlingDataIntegrityViolationException accessing " + requestUrl + " with message " + e.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("fragments/message");
        modelAndView.addObject("message", e.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.EXPECTATION_FAILED)
    public ModelAndView handlingException(Exception e, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String url = httpServletRequest.getRequestURL().toString();
        String queryString = httpServletRequest.getQueryString();
        String requestUrl = url + (Utils.nonNull(queryString) ? "?" + queryString : "");
        log.error("Error in handlingException accessing " + requestUrl + " with message " + e.getMessage());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("fragments/message");
        modelAndView.addObject("message", e.getMessage());
        return modelAndView;
    }
}
