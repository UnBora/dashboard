package com.boraun.dashboard.admin.authority;

import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.user.AdminUserEntity;
import com.boraun.dashboard.admin.user.AdminUserRepository;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import com.boraun.dashboard.common.annotation.RootAuthority;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminAuthorityService extends WebAdminBaseService<AdminAuthorityEntity, Long> {

    private final AdminAuthorityRepository repository;
    private static final int LENGTH = 200;
    private final AdminUserRepository adminUserRepository;

    @Autowired
    private AuditLogService auditLogService;

    protected AdminAuthorityService(AdminAuthorityRepository repository, AdminUserRepository adminUserRepository) {
        super(AdminAuthorityEntity.class, repository);
        this.repository = repository;
        this.adminUserRepository = adminUserRepository;
    }

    public AdminAuthorityEntity findByEndPointUrl(String endPointUrl) {
        return repository.findFirstByEndPointUrl(endPointUrl).orElse(null);
    }

    public List<AdminAuthorityEntity> findAllByStatus(CoreConstants.Status status, Comparator<AdminAuthorityEntity> comparator) {
        List<AdminAuthorityEntity> adminAuthorityEntities = repository.findAllByStatus(status);
        for (AdminAuthorityEntity nav : adminAuthorityEntities) {
            nav.setHasSub(repository.findAllByParent_IdAndStatus(nav.getId(), CoreConstants.Status.Enabled).size()!= 0);
        }
        if (comparator != null) {
            adminAuthorityEntities.sort(comparator);
        }
        return adminAuthorityEntities;
    }

    @Override
    public Pager<AdminAuthorityEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> status, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.ASC, "createdAt");
        Page<AdminAuthorityEntity> entities = repository.findAllByAuthorityNameContainingIgnoreCaseAndStatusIn(pageable, search.orElse(""), status);
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities);
    }

    @Override
    public AdminAuthorityEntity update(Long aLong, AdminAuthorityEntity entity) {
        AdminAuthorityEntity logObject = new AdminAuthorityEntity();
        AdminAuthorityEntity adminAuthorityEntity = repository.findById(aLong).orElse(null);
        if (adminAuthorityEntity != null) {

            BeanUtils.copyProperties(adminAuthorityEntity, logObject);
            adminAuthorityEntity.setAuthorityName(entity.getAuthorityName());
            adminAuthorityEntity.setParent(entity.getParent());
            adminAuthorityEntity.setFaIcon(entity.getFaIcon());
            adminAuthorityEntity.setAuthorityOrder(entity.getAuthorityOrder());
            adminAuthorityEntity.setMenu(entity.isMenu());
            adminAuthorityEntity.setEndPointUrl(entity.getEndPointUrl());

            repository.saveAndFlush(adminAuthorityEntity);

            //audit log
            auditLogService.save(logObject, adminAuthorityEntity, AuditLogConstants.AuditLogType.UPDATED);
            return adminAuthorityEntity;
        }
        return null;
    }

    @Override
    public AdminAuthorityEntity add(AdminAuthorityEntity entity) {
        return null;
    }

    public List<AdminAuthorityEntity> rescanAuthority(ConfigurableApplicationContext context) {
        try {
            String[] beanNames = context.getBeanDefinitionNames();
            List<String> excludedEndpoints = Arrays.asList(
                    "/web/admin/authentication",
                    "${server.error.path:${error.path:/error}}",
                    "/web/permit",
                    "/web/admin/unauthorized",
                    "/resources",
                    "/web/admin/request-object",
                    "/web/admin/language",
                    "/web/permit/email"
            );
            List<AdminAuthorityEntity> authorityEntities = new ArrayList<>();
            AdminAuthorityEntity rootAuthority = null, authority = null, subAuthority = null;
            for (String aBeanName : beanNames) {
                Class<?> clazz = context.getBean(aBeanName).getClass();
                String clazzName = clazz.getName().split("\\$\\$")[0];
                if (clazzName.endsWith("Controller")) {
                    clazz = Class.forName(clazzName);
                    Annotation[] annotations = clazz.getAnnotations();
                    for (Annotation annotation : annotations) {
                        //get root authority
                        if (annotation instanceof RootAuthority rootAuthorityAnnotation) {
//                            if (authorityEntities.stream().noneMatch(x -> Objects.equals(x.getAuthorityName(), rootAuthorityAnnotation.name()))) {
                            rootAuthority = repository.findAdminAuthorityEntityByAuthorityNameAndMenuIsTrue(rootAuthorityAnnotation.name()).orElse(null);
                            if (rootAuthority == null) {
                                rootAuthority = new AdminAuthorityEntity();
                                rootAuthority.setAuthorityKey(RandomStringUtils.randomAlphanumeric(LENGTH));
                                rootAuthority.setAuthorityName(rootAuthorityAnnotation.name());
                                rootAuthority.setParent(null);
                                rootAuthority.setMenu(true);
                                rootAuthority.setAuthorityOrder(0);
                                rootAuthority.setFaIcon(rootAuthorityAnnotation.icon());
                                rootAuthority.setEndPointUrl("javascript:void(0);");
                                rootAuthority.setCreatedBy("System");
                                rootAuthority.setStatus(CoreConstants.Status.Enabled);
                                rootAuthority = repository.save(rootAuthority);
                                authorityEntities.add(rootAuthority);
                            }
                        }
                        String url, name;
                        if (annotation instanceof RequestMapping mapping) {
                            url = mapping.value()[0];
                            name = mapping.name();
                        } else if (annotation instanceof GetMapping mapping) {
                            url = mapping.value()[0];
                            name = mapping.name();
                        } else if (annotation instanceof PostMapping mapping) {
                            url = mapping.value()[0];
                            name = mapping.name();
                        } else {
                            url = "";
                            name = "";
                        }
                        if (Utils.isNull(url) || excludedEndpoints.contains(url)) {
                            continue;
                        }
                        //if (authorityEntities.stream().noneMatch(x -> Objects.equals(x.getEndPointUrl(), url))) {
                        authority = repository.findAdminAuthorityEntityByEndPointUrl(url).orElse(null);
                        if (authority == null) {
                            authority = new AdminAuthorityEntity();
                            authority.setAuthorityKey(RandomStringUtils.randomAlphanumeric(LENGTH));
                            authority.setAuthorityName(name);
                            authority.setParent(rootAuthority);
                            authority.setMenu(true);
                            authority.setAuthorityOrder(0);
                            authority.setFaIcon("");
                            authority.setEndPointUrl(url);
                            authority.setCreatedBy("System");
                            authority.setStatus(CoreConstants.Status.Enabled);
                            authority = repository.save(authority);
                            authorityEntities.add(authority);
                        }
//                        System.out.println("Controller " + clazzName + ", Name = " + name + ", Url = " + url + ", Root = " + rootName);
                        //get sub authority
                        Method[] methods = clazz.getMethods();
                        for (Method method : methods) {
                            Annotation[] subAnnotations = method.getAnnotations();
                            for (Annotation subAnnotation : subAnnotations) {
                                String subUrl, subName;
                                if (subAnnotation instanceof RequestMapping mapping) {
                                    subUrl = mapping.value().length > 0 ? mapping.value()[0] : "";
                                    subName = mapping.name();
                                } else if (subAnnotation instanceof GetMapping mapping) {
                                    subUrl = mapping.value().length > 0 ? mapping.value()[0] : "";
                                    subName = mapping.name();
                                } else if (subAnnotation instanceof PostMapping mapping) {
                                    subUrl = mapping.value().length > 0 ? mapping.value()[0] : "";
                                    subName = mapping.name();
                                } else {
                                    subName = "";
                                    subUrl = "";
                                }

                                subUrl = StringUtils.replace(subUrl, "{id}", "*");

                                if (Utils.nonNull(subUrl) && Utils.nonNull(subName)) {
                                    //if (authorityEntities.stream().noneMatch(x -> Objects.equals(x.getEndPointUrl(), url + subUrl))) {
                                    subAuthority = repository.findAdminAuthorityEntityByEndPointUrl(url + subUrl).orElse(null);
                                    if (subAuthority == null) {
                                        subAuthority = new AdminAuthorityEntity();
                                        subAuthority.setAuthorityKey(RandomStringUtils.randomAlphanumeric(LENGTH));
                                        subAuthority.setAuthorityName(subName);
                                        subAuthority.setParent(authority);
                                        subAuthority.setMenu(false);
                                        subAuthority.setAuthorityOrder(0);
                                        subAuthority.setFaIcon("");
                                        subAuthority.setEndPointUrl(url + subUrl);
                                        subAuthority.setCreatedBy("System");
                                        subAuthority.setStatus(CoreConstants.Status.Enabled);
                                        subAuthority = repository.save(subAuthority);
                                        authorityEntities.add(subAuthority);
                                    }
                                    //System.out.println("SubUrl = " + subUrl + ", SubName = " + subName);
                                }
                            }
                        }
                    }
                }
            }
            //add log here
            if (!authorityEntities.isEmpty()) {
                authorityEntities.forEach(a -> {
                    log.info("Added Authority " + StringUtils.rightPad(a.getAuthorityName(), 40, " ") + a.getEndPointUrl());
                });
                AdminAuthorityEntity findDashboard = repository.findAdminAuthorityEntityByAuthorityName("Dashboard").orElse(null);
                if (findDashboard != null && findDashboard.getParent() != null) {
                    findDashboard.setParent(null);
                    findDashboard.setFaIcon("fa-solid fa-gauge");
                    repository.saveAndFlush(findDashboard);
                    log.info("Updated Dashboard " + StringUtils.rightPad(findDashboard.getAuthorityName(), 40, " ") + findDashboard.getEndPointUrl());
                }
            } else {
                log.info("No New Authority Added");
            }
            return authorityEntities;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AdminAuthorityEntity> findAuthoritiesByUsername(String username, Comparator comparator) {
        AdminUserEntity user = adminUserRepository.findAdminUserEntityByUsername(username).orElseThrow();
        if (Objects.isNull(user.getAdminRoleEntity())) throw new NullPointerException();
        List<AdminAuthorityEntity> authorityEntities = user.getAdminRoleEntity().getAdminAuthorityEntities().stream()
                .sorted(Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder))
                .collect(Collectors.toList());
        authorityEntities.removeIf(x -> x.getStatus() != CoreConstants.Status.Enabled);
        for (AdminAuthorityEntity authority : authorityEntities) {
            authority.setHasSub(!repository.findAllByParent_IdAndStatus(authority.getId(), CoreConstants.Status.Enabled).isEmpty());
        }
        if (comparator != null)
            authorityEntities.sort(comparator);
        authorityEntities.removeIf(x -> x.getStatus() != CoreConstants.Status.Enabled);
        return authorityEntities;
    }

//    public List<AdminAuthorityEntity> findAuthoritiesByUsername(String username) {
//        AdminUserEntity user = adminUserRepository.findAdminUserEntityByUsername(username).orElseThrow();
//        if (Objects.isNull(user.getAdminRoleEntity())) throw new NullPointerException();
//        List<AdminAuthorityEntity> authorityEntities = user.getAdminRoleEntity().getAdminAuthorityEntities().stream()
//                .sorted(Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder))
//                .collect(Collectors.toList());
//        authorityEntities.removeIf(x -> x.getStatus() != CoreConstants.Status.Enabled);
//        for (AdminAuthorityEntity authority : authorityEntities) {
//            AdminAuthorityEntity entity = repository.findAdminAuthorityEntityByAuthorityNameAndMenuIsTrue(authority.getAuthorityName()).orElse(null);
//            if (entity == null) {
//                authority.setHasSub(false);
//            } else {
//                if (entity.getStatus() == CoreConstants.Status.Enabled && !Objects.equals(entity.getId(), authority.getId())) {
//                    authority.setHasSub(true);
//                } else {
//                    authority.setHasSub(false);
//                }
//            }
//        }
//        return authorityEntities;
//    }

    @Override
    public void delete(Long aLong) {
        AdminAuthorityEntity entity = repository.findById(aLong).orElse(null);
        if (entity != null) {
            entity.setStatus(CoreConstants.Status.Deleted);
            repository.saveAndFlush(entity);
        }
    }
    List<AdminAuthorityEntity> findAllByMenu(boolean isMenu, Comparator comparator) {
        List<AdminAuthorityEntity> navigationEntities = repository.findAllByMenuIs(isMenu);
        if (!ObjectUtils.isEmpty(comparator))
            navigationEntities.sort(comparator);
        return navigationEntities;
    }

    public List<AdminAuthorityEntity> saveNavigationOrder(String navigationOrderAsString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<AdminAuthorityOrderModel> navigationOrderModel = objectMapper.readValue(navigationOrderAsString, new TypeReference<List<AdminAuthorityOrderModel>>() {
            });

            List<AdminAuthorityEntity> adminNavigationEntities = findAllByStatus(CoreConstants.Status.Enabled, Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder));

            int order = 0;
            for (int i = 0; i < navigationOrderModel.size(); i++) {
                List<SubAuthorityOrderModel> subNavigationOrderModels = navigationOrderModel.get(i).getChildren();
                if (Objects.isNull(subNavigationOrderModels)) subNavigationOrderModels = new ArrayList<>();
                for (int j = 0; j < subNavigationOrderModels.size(); j++) {
                    List<SubAuthorityOrderModel> subNavigationOrderModels1 = subNavigationOrderModels.get(j).getChildren();
                    if (Objects.isNull(subNavigationOrderModels1)) subNavigationOrderModels1 = new ArrayList<>();
                    for (int k = 0; k < subNavigationOrderModels1.size(); k++) {
                        Long subId = subNavigationOrderModels1.get(k).getId();
                        order = order + 1;
                        Long parentId = subNavigationOrderModels.get(j).getId();
                        int finalOrder = order;
                        adminNavigationEntities.stream().filter(x -> x.getId().longValue() == subId.longValue()).forEach(adminNavigationEntity -> {
                            adminNavigationEntity.setAuthorityOrder(finalOrder);
                            adminNavigationEntity.setParent(new AdminAuthorityEntity(parentId));
                        });
                    }
                    Long subId = subNavigationOrderModels.get(j).getId();
                    order = order + 1;
                    Long parentId = navigationOrderModel.get(i).getId();
                    int finalOrder1 = order;
                    adminNavigationEntities.stream().filter(x -> x.getId().longValue() == subId.longValue()).forEach(adminNavigationEntity -> {
                        adminNavigationEntity.setAuthorityOrder(finalOrder1);
                        adminNavigationEntity.setParent(new AdminAuthorityEntity(parentId));
                    });
                }
                Long subId = navigationOrderModel.get(i).getId();
                order = order + 1;
                int finalOrder1 = order;
                adminNavigationEntities.stream().filter(x -> x.getId().longValue() == subId.longValue()).forEach(adminNavigationEntity -> {
                    adminNavigationEntity.setAuthorityOrder(finalOrder1);
                    adminNavigationEntity.setParent(null);
                });
            }
            save(adminNavigationEntities);
            return adminNavigationEntities;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public AdminAuthorityEntity save(AdminAuthorityEntity navigationEntity) {
        return repository.saveAndFlush(navigationEntity);
    }
    public void save(List<AdminAuthorityEntity> adminAuthorityEntities) {
        adminAuthorityEntities.forEach(this::save);
    }

    public List<AdminAuthorityEntity> findAllByParentIdAndStatusAndMenuIs(Long parentId, CoreConstants.Status status, boolean menu) {
        return repository.findAllByParent_IdAndStatusAndMenuIs(parentId, status, menu);
    }

}
