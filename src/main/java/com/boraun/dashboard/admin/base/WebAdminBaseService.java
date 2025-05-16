package com.boraun.dashboard.admin.base;



import com.boraun.dashboard.common.BaseEntity;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
public abstract class WebAdminBaseService<T, ID> {

    final Class<T> typeParameterClass;

    final JpaRepository<T, ID> repository;

    protected WebAdminBaseService(Class<T> typeParameterClass, JpaRepository<T, ID> repository) {
        this.typeParameterClass = typeParameterClass;
        this.repository = repository;
    }

    public T newInstance() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return typeParameterClass.getConstructor().newInstance();
    }


    public abstract Pager<T> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> status, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters);

    public T findOne(ID id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(ID id) {
        repository.deleteById(id);
    }

    public abstract T update(ID id, T entity);

    public T toggleStatus(ID id) {
        T t = this.findOne(id);
        if (t != null) {
            BaseEntity baseEntity = (BaseEntity) t;
            CoreConstants.Status status = baseEntity.getStatus() == CoreConstants.Status.Enabled ? CoreConstants.Status.Disabled : CoreConstants.Status.Enabled;
            baseEntity.setStatus(status);
            return this.saveAndFlush(t);
        }
        return null;
    }

    public T add(T entity) {
        return repository.save(entity);
    }

    public T saveAndFlush(T entity) {
        return repository.saveAndFlush(entity);
    }

}
