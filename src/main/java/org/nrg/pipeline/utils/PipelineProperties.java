/*
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 *
 */

package org.nrg.pipeline.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: PipelineProperties.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class PipelineProperties {

    public static void init(String configFilePath) throws Exception {
        if (!_initialized) {
            _log.debug("Initializing the pipeline properties from the file: " + configFilePath);
            Properties properties = new Properties();
            properties.load(new FileInputStream(configFilePath));
            init(properties);
        }
    }

    public static void init(Properties properties) {
        _log.debug("Initializing the pipeline properties from the specified properties object");
        if (properties != null && properties.size() > 0) {
            if (properties.containsKey("ADMIN_EMAIL")) {
                _pipelineAdminEmail = properties.getProperty("ADMIN_EMAIL");
                _log.debug("Setting admin email to: " + _pipelineAdminEmail);
            }
            if (properties.containsKey("PIPELINE_EMAIL_ID")) {
                _pipelineEmailId = properties.getProperty("PIPELINE_EMAIL_ID");
                _log.debug("Setting email ID to: " + _pipelineEmailId);
            }
            if (properties.containsKey("PIPELINE_SMTP_HOST")) {
                _pipelineSmtpHost = properties.getProperty("PIPELINE_SMTP_HOST");
                _log.debug("Setting SMTP host to: " + _pipelineSmtpHost);
            }
            if (properties.containsKey("PIPELINE_SMTP_USER")) {
                _pipelineSmtpUser = properties.getProperty("PIPELINE_SMTP_USER");
                _log.debug("Setting SMTP user to: " + _pipelineSmtpUser);
            }
            if (properties.containsKey("PIPELINE_SMTP_PASS")) {
                _pipelineSmtpPass = properties.getProperty("PIPELINE_SMTP_PASS");
                _log.debug("Setting SMTP password to secure value");
            }
            if (properties.containsKey("PIPELINE_REST_MAIL_SVC")) {
                _pipelineRestMailSvc = properties.getProperty("PIPELINE_REST_MAIL_SVC");
                _log.debug("Setting REST mail host to: " + _pipelineRestMailSvc);
            }
            if (properties.containsKey("PIPELINE_REST_MAIL_USER")) {
                _pipelineRestMailUser = properties.getProperty("PIPELINE_REST_MAIL_USER");
                _log.debug("Setting REST mail user to: " + _pipelineRestMailUser);
            }
            if (properties.containsKey("PIPELINE_REST_MAIL_PASS")) {
                _pipelineRestMailPass = properties.getProperty("PIPELINE_REST_MAIL_PASS");
                _log.debug("Setting REST mail password to secure value");
            }
            if (properties.containsKey("PIPELINE_CATALOG_ROOT_PATH")) {
                _pipelineCatalogRootPath = properties.getProperty("PIPELINE_CATALOG_ROOT_PATH");
                if (!_pipelineCatalogRootPath.endsWith(File.separator)) {
               		_pipelineCatalogRootPath = _pipelineCatalogRootPath + File.separator;
                    _log.debug("Setting catalog root path to: " + _pipelineCatalogRootPath);
               	}
            }
            _initialized = true;
        }
    }

    public static String getPipelineAdminEmail() {
        if (!_initialized) {
            throw new RuntimeException("The pipeline properties have not been initialized!");
        }
        return _pipelineAdminEmail;
    }

    public static String getPipelineEmail() {
        if (!_initialized) {
            throw new RuntimeException("The pipeline properties have not been initialized!");
        }
        return _pipelineEmailId;
    }

    public static String getPipelineSmtpHost() {
        if (!_initialized) {
            throw new RuntimeException("The pipeline properties have not been initialized!");
        }
        return _pipelineSmtpHost;
    }

    public static String getPipelineSmtpUser() {
        if (!_initialized) {
            throw new RuntimeException("The pipeline properties have not been initialized!");
        }
        return _pipelineSmtpUser;
    }

    public static String getPipelineSmtpPass() {
        if (!_initialized) {
            throw new RuntimeException("The pipeline properties have not been initialized!");
        }
        return _pipelineSmtpPass;
    }

    public static String getPipelineRestMailService() {
        if (!_initialized) {
            throw new RuntimeException("The pipeline properties have not been initialized!");
        }
        return _pipelineRestMailSvc;
    }

    public static String getPipelineRestMailUser() {
        if (!_initialized) {
            throw new RuntimeException("The pipeline properties have not been initialized!");
        }
        return _pipelineRestMailUser;
    }

    public static String getPipelineRestMailPassword() {
        if (!_initialized) {
            throw new RuntimeException("The pipeline properties have not been initialized!");
        }
        return _pipelineRestMailPass;
    }

    public static String getPipelineCatalogRootPath() {
        if (!_initialized) {
            throw new RuntimeException("The pipeline properties have not been initialized!");
        }
        return _pipelineCatalogRootPath;
    }

    private static final Log _log = LogFactory.getLog(PipelineProperties.class);

    private static boolean _initialized = false;

    private static String _pipelineAdminEmail = null;
    private static String _pipelineEmailId = null;
    private static String _pipelineSmtpHost = null;
    private static String _pipelineSmtpUser = null;
	private static String _pipelineSmtpPass = null;
    private static String _pipelineRestMailSvc = null;
    private static String _pipelineRestMailUser = null;
    private static String _pipelineRestMailPass = null;
    private static String _pipelineCatalogRootPath = null;
}
