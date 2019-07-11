package com.ido.luffy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.ido.luffy.SecurityManager.ADMIN_ROLE;

/**
 * Auto config for Luffy
 */
@Slf4j
@Import(LuffyAutoConfig.class)
public class LuffyConfig implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private ResourceLoader resourceLoader;

    private Environment environment;
    static Map<String, Set<String>> rolesUrlTable = new HashMap<>(20);


    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (log.isDebugEnabled()) {
            log.debug(" register bean definition by import only");
        }

        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        Set<String> basePackages;

        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(
                RestController.class);
        scanner.addIncludeFilter(annotationTypeFilter);

        basePackages = getBasePackages(metadata);


        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner
                    .findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    Map<String, Object> ctrlAttr = annotationMetadata.getAnnotationAttributes(RequestMapping.class.getName());
                    Set<String> handledMethods = new HashSet<>();
                    //only support request mapping for one url
                    String path = ((String[]) ctrlAttr.get("value"))[0];
                    if (path == null) {
                        path = (String) ctrlAttr.get("path");
                    }

                    Set<MethodMetadata> getMethods = annotationMetadata.getAnnotatedMethods(GetMapping.class.getName());
                    for (MethodMetadata me : getMethods) {
                        handledMethods.add(me.getMethodName());
                        Map<String, Object> attr = me.getAnnotationAttributes(GetMapping.class.getName());
                        fillRoleUrlTable(path, me, attr);

                    }

                    Set<MethodMetadata> postMethod = annotationMetadata.getAnnotatedMethods(PostMapping.class.getName());
                    for (MethodMetadata me : postMethod) {
                        handledMethods.add(me.getMethodName());
                        Map<String, Object> attr = me.getAnnotationAttributes(PostMapping.class.getName());
                        fillRoleUrlTable(path, me, attr);

                    }

                    Set<MethodMetadata> deleteMethods = annotationMetadata.getAnnotatedMethods(DeleteMapping.class.getName());
                    for (MethodMetadata me : deleteMethods) {
                        handledMethods.add(me.getMethodName());
                        Map<String, Object> attr = me.getAnnotationAttributes(DeleteMapping.class.getName());
                        fillRoleUrlTable(path, me, attr);

                    }

                    Set<MethodMetadata> requestMethod = annotationMetadata.getAnnotatedMethods(RequestMapping.class.getName());
                    for (MethodMetadata me : requestMethod) {
                        if (handledMethods.contains(me.getMethodName())) {
                            continue;
                        }
                        Map<String, Object> attr = me.getAnnotationAttributes(RequestMapping.class.getName());
                        fillRoleUrlTable(path, me, attr);

                    }

                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("role permission table is {}", rolesUrlTable.toString());
        }


    }


    /**
     * @param path
     * @param me
     * @param attr Get Post Mapping annotation attr
     */
    private void fillRoleUrlTable(String path, MethodMetadata me, Map<String, Object> attr) {
        String[] roles = getMethodRequireRoles(me);
        String url = ((String[]) attr.get("value"))[0];
        final String fullUrl = path + "/" + url;
        if (roles == null) {
            //default all url is accessible to admin
            Set<String> adPermission = rolesUrlTable.get(ADMIN_ROLE);
            if (adPermission == null) {
                adPermission = new HashSet<>(50);
            }
            adPermission.add(fullUrl);
            rolesUrlTable.put(ADMIN_ROLE, adPermission);
            return;
        }

        for (String r : roles) {
            Set<String> set = rolesUrlTable.get(r);
            if (set == null) {
                set = new HashSet<>();
                set.add(fullUrl);
                rolesUrlTable.put(r, set);
            } else {
                set.add(fullUrl);
            }
        }

    }

    private String[] getMethodRequireRoles(MethodMetadata me) {
        Map<String, Object> roleAttr = me.getAnnotationAttributes(RequireRoles.class.getName());
        if (roleAttr == null) {
            return null;
        }
        String[] roles = (String[]) roleAttr.get("value");
        return roles;
    }

    public static void main(String[] args) {
        System.out.println(RequestMapping.class.getName());
    }


    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableLuffy.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();

        basePackages.addAll(Arrays.asList((String[]) attributes.get("basePackages")));


        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
