package com.dotcms.osgi.oauth;

import com.dotcms.filters.interceptor.FilterWebInterceptorProvider;
import com.dotcms.filters.interceptor.WebInterceptorDelegate;
import com.dotcms.osgi.oauth.configuration.PluginConfigurator;
import com.dotcms.osgi.oauth.dotcms.DotCmsFacade;
import com.dotcms.osgi.oauth.dotcms.DotCmsService;
import com.dotcms.osgi.oauth.interceptor.*;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.filters.AutoLoginFilter;
import com.dotmarketing.filters.LoginRequiredFilter;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.osgi.framework.BundleContext;

import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.util.Logger;

public class Activator extends GenericBundleActivator {

    // This file path is the location of the default properties file within the OSGi bundle.
    private static final String DEFAULT_PLUGIN_CONFIGURATION_FILE_PATH = "/oauth-plugin-default-configuration.properties";
    private static final String EXTERNAL_CONFIGURATION_FILE_NAME = "oauth-plugin-external-configuration.properties";
    private static final String OAUTH_PROVIDER_CONFIGURATION_PROPERTIES_FILE_NAME = "oauth-provider.properties";

    private BackEndLoginRequiredInterceptor backEndLoginRequiredInterceptor;
    private FrontEndLoginRequiredInterceptor frontEndLoginRequiredInterceptor;
    private OAuthCallbackInterceptor oAuthCallbackInterceptor;
    private BackEndLogoutInterceptor backEndLogoutInterceptor;
    private FrontEndLogoutInterceptor frontEndLogoutInterceptor;

    private boolean backEndLoginEnabled;
    private LoggerContext pluginLoggerContext;

    @SuppressWarnings("unchecked")
    public void start(org.osgi.framework.BundleContext context) throws Exception {
        initializeLogger();
        configurePlugin();
        this.initializeServices(context);
        registerOAuthInterceptors();
    }

    private void initializeLogger() {
        //Initializing log4j...
        LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();
        
        //Initializing the log4j context of this plugin based on the dotCMS logger context
        pluginLoggerContext = (LoggerContext) LogManager.getContext(this.getClass().getClassLoader(),
                false,
                dotcmsLoggerContext,
                dotcmsLoggerContext.getConfigLocation());
    }
    
    private void configurePlugin() {
        Logger.info(this.getClass(), "Configuring OAuth Plugin..");

        DotCmsFacade dotCmsFacade = new DotCmsService(
                APILocator.getUserAPI(),
                APILocator.getRoleAPI(),
                APILocator.getLoginServiceAPI()
        );

        PluginConfigurator pluginConfigurator = new PluginConfigurator(
                dotCmsFacade,
                DEFAULT_PLUGIN_CONFIGURATION_FILE_PATH,
                EXTERNAL_CONFIGURATION_FILE_NAME,
                OAUTH_PROVIDER_CONFIGURATION_PROPERTIES_FILE_NAME);

        pluginConfigurator.initializeObjectGraphDependencies();
        Logger.info(this.getClass(), "Configuration Complete. Starting interceptors..");

        backEndLoginEnabled = pluginConfigurator.getBackEndLoginEnabledFeatureFlag();
        Logger.info(this, "The BackEndLoginEnabled feature flag is set to: " + backEndLoginEnabled);

        if (backEndLoginEnabled) {
            backEndLoginRequiredInterceptor = pluginConfigurator.createBackEndLoginRequiredInterceptor();
            backEndLogoutInterceptor = pluginConfigurator.createBackEndLogoutInterceptor();
        }

        frontEndLoginRequiredInterceptor = pluginConfigurator.createFrontEndLoginRequiredInterceptor();
        oAuthCallbackInterceptor = pluginConfigurator.createOAuthCallbackInterceptor();
        frontEndLogoutInterceptor = pluginConfigurator.createFrontEndLogoutInterceptor();
    }


    @Override
    public void stop(BundleContext context) throws Exception {
        Log4jUtil.shutdown(pluginLoggerContext);
        unregisterServices(context);
        removeInterceptors();
    }


    private void registerOAuthInterceptors() {
        final FilterWebInterceptorProvider filterWebInterceptorProvider = FilterWebInterceptorProvider
                .getInstance(Config.CONTEXT);

        final WebInterceptorDelegate loginRequiredDelegate = filterWebInterceptorProvider
                .getDelegate(LoginRequiredFilter.class);

        if (null != loginRequiredDelegate) {

            if (backEndLoginEnabled) {
                Logger.info(this.getClass(), "Adding the BackEndLoginRequiredInterceptor");
                loginRequiredDelegate.addFirst(backEndLoginRequiredInterceptor);
            }

            Logger.info(this.getClass(), "Adding the FrontEndLoginRequiredInterceptor");
            loginRequiredDelegate.addFirst(frontEndLoginRequiredInterceptor);
        }

        final WebInterceptorDelegate autoLoginDelegate = filterWebInterceptorProvider
                .getDelegate(AutoLoginFilter.class);
        if (null != autoLoginDelegate) {

            if (backEndLoginEnabled) {
                Logger.info(this.getClass(), "Adding the BackEndLogoutInterceptor");
                autoLoginDelegate.addFirst(backEndLogoutInterceptor);
            }

            Logger.info(this.getClass(), "Adding the FrontEndLogoutInterceptor");
            autoLoginDelegate.addFirst(frontEndLogoutInterceptor);

            Logger.info(this.getClass(), "Adding the OAuthCallbackInterceptor");
            autoLoginDelegate.addFirst(oAuthCallbackInterceptor);
        }
    }


    private void removeInterceptors() {
        final FilterWebInterceptorProvider filterWebInterceptorProvider = FilterWebInterceptorProvider
                .getInstance(Config.CONTEXT);

        final WebInterceptorDelegate loginRequiredDelegate = filterWebInterceptorProvider
                .getDelegate(LoginRequiredFilter.class);
        if (null != backEndLoginRequiredInterceptor && null != frontEndLoginRequiredInterceptor && null != loginRequiredDelegate) {

            if (backEndLoginEnabled) {
                Logger.info(this.getClass(), "Removing the BackEndLoginRequiredInterceptors");
                loginRequiredDelegate.remove(backEndLoginRequiredInterceptor.getName(), true);
            }

            Logger.info(this.getClass(), "Removing the FrontEndLoginRequiredInterceptors");
            loginRequiredDelegate.remove(frontEndLoginRequiredInterceptor.getName(), true);
        }

        final WebInterceptorDelegate autoLoginDelegate = filterWebInterceptorProvider
                .getDelegate(AutoLoginFilter.class);
        if (null != oAuthCallbackInterceptor && null != autoLoginDelegate) {
            Logger.info(this.getClass(), "Removing the OAuthCallbackInterceptor");
            autoLoginDelegate.remove(oAuthCallbackInterceptor.getName(), true);
        }

        if (backEndLoginEnabled) {
            if (null != backEndLogoutInterceptor && null != autoLoginDelegate) {
                Logger.info(this.getClass(), "Removing the BackEndLogoutInterceptor");
                autoLoginDelegate.remove(backEndLogoutInterceptor.getName(), true);
            }
        }

        if (null != frontEndLogoutInterceptor && null != autoLoginDelegate) {
            Logger.info(this.getClass(), "Removing the FrontEndLogoutInterceptor");
            autoLoginDelegate.remove(frontEndLogoutInterceptor.getName(), true);
        }
    }
}
