package com.sag.eiti.config;

import akka.actor.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpringExtension extends AbstractExtensionId<SpringExtension.SpringExt> {

    private static SpringExtension instance = new SpringExtension();

    @Override
    public SpringExt createExtension(ExtendedActorSystem system) {
        return new SpringExt();
    }

    public static SpringExtension getInstance() {
        return instance;
    }

    public static class SpringExt implements Extension {

        private static ApplicationContext applicationContext;

        public void initialize(ApplicationContext applicationContext) {
            SpringExt.applicationContext = applicationContext;
        }

        Props props(Class<? extends Actor> actorBeanClass) {
            return Props.create(SpringActorProducer.class, applicationContext, actorBeanClass);
        }

        Props props(Class<? extends Actor> actorBeanClass, Object... parameters) {
            return Props.create(SpringActorProducer.class, applicationContext, actorBeanClass, parameters);
        }
    }

}
