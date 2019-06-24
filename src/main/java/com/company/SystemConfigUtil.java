package com.company;

import com.aisera.common.config.DynamicProperties;
import com.aisera.common.config.SystemConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SystemConfigUtil {

  private static final Logger logger = LoggerFactory.getLogger(SystemConfigUtil.class);
  private static SystemConfigUtil instance;
  private List<SystemConfig.SystemConfigMapper> config;
  ObjectMapper mapper = new ObjectMapper();
  private static final String TENANT_CONFIG_KEY = "tenantConfig";

  private SystemConfigUtil() throws Exception {
    String json = com.aisera.common.config.SystemConfig.defaultConfig;
    config = Arrays.asList(mapper.readValue(json, com.aisera.common.config.SystemConfig.SystemConfigMapper[].class));
  }

  public static SystemConfigUtil getInstance() {
    if (instance != null)
      return instance;

    synchronized (SystemConfigUtil.class) {
      if (instance == null) {
        try {
          instance = new SystemConfigUtil();
        } catch (Exception e) {
          instance = null;
          logger.error("Failed to load system configuration.", e);
        }
      }
      return instance;
    }
  }


  public List<SystemConfig.SystemConfigMapper> getConfig() {
    return config;
  }

  public List<SystemConfig.SystemConfigMapper> getConfigCopy() throws Exception {
    String json = com.aisera.common.config.SystemConfig.defaultConfig;
    return Arrays.asList(mapper.readValue(json, com.aisera.common.config.SystemConfig.SystemConfigMapper[].class));
  }

  private JsonNode getSavedJson(String tenantId) throws Exception {
    try {
      String jsonString = getSavedVal(tenantId);
      if (StringUtils.isBlank(jsonString))
        return new ObjectMapper().readTree("{}");

      return new ObjectMapper().readTree(jsonString);
    } catch (Exception e) {
      logger.error("Failed to deserialize saved config for reason: {}", e);
      return new ObjectMapper().readTree("{}");
    }
  }

  private String getSavedVal(String tenantId) throws Exception {
    AtomicReference<String> val = new AtomicReference<>();
    try {
      DynamicProperties.getInstance().ifPresent(instance -> instance.getProperty(tenantId, TENANT_CONFIG_KEY)
        .ifPresent(item -> val.set(item)));
    } catch (Exception e) {
    }
    return val.get();
  }

  private void saveConfigJson(String tenantId, String key, String json) {
    DynamicProperties.getInstance().ifPresent(instance -> {
      try {
        instance.setProperty(tenantId, key, json);
      } catch (Exception e) {
      }
    });
  }

  public String getDefaultConfigJson() throws Exception {
    List<SystemConfig.SystemConfigMapper> configs = getConfig();
    return getConfigJson(configs);
  }

  public String getConfigJson(List<SystemConfig.SystemConfigMapper> configs) throws Exception {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getConfigJsonObject(configs));
  }

  private ObjectNode getConfigJsonObject(List<SystemConfig.SystemConfigMapper> configs) throws Exception {
    try {

      ObjectNode objNode = mapper.createObjectNode();

      configs.stream().forEach(item -> {
        item.getFields().stream().forEach(field -> {
          if (field instanceof SystemConfig.SystemConfigMapper.IntegerField) {
            objNode.put(field.getField(), ((SystemConfig.SystemConfigMapper.IntegerField) field).getDefaultValue());

          } else if (field instanceof SystemConfig.SystemConfigMapper.BoolField) {
            objNode.put(field.getField(), ((SystemConfig.SystemConfigMapper.BoolField) field).isDefaultValue());

          } else if (field instanceof SystemConfig.SystemConfigMapper.StringField) {
            objNode.put(field.getField(), ((SystemConfig.SystemConfigMapper.StringField) field).getDefaultValue());

          } else if (field instanceof SystemConfig.SystemConfigMapper.DecimalField) {
            objNode.put(field.getField(), ((SystemConfig.SystemConfigMapper.DecimalField) field).getDefaultValue());

          } else if (field instanceof SystemConfig.SystemConfigMapper.IntegerList) {
            objNode.putPOJO(field.getField(), ((SystemConfig.SystemConfigMapper.IntegerList) field).getDefaultValue());

          } else if (field instanceof SystemConfig.SystemConfigMapper.OntologyList) {
            objNode.putPOJO(field.getField(), ((SystemConfig.SystemConfigMapper.OntologyList) field).getDefaultValue());


          } else if (field instanceof SystemConfig.SystemConfigMapper.SearchTypeList) {
            objNode.putPOJO(field.getField(), ((SystemConfig.SystemConfigMapper.SearchTypeList) field).getDefaultValue());

          } else if (field instanceof SystemConfig.SystemConfigMapper.SearchTypeWeightList) {
            objNode.putPOJO(field.getField(), ((SystemConfig.SystemConfigMapper.SearchTypeWeightList) field).getDefaultValue());
          }
        });
      });

      return objNode;
    } catch (Exception e) {
      logger.error("Failed to build deafult configuration JSON for reason: {}", e);
      throw e;
    }
  }

  public List<SystemConfig.SystemConfigMapper> getTenantConfig(String tenantId) throws Exception {
    List<SystemConfig.SystemConfigMapper> newConfigs = getConfigCopy();
    try {
      JsonNode savedConfig = getSavedJson(tenantId);
      applySettingsToConfig(savedConfig, newConfigs);
    } catch (Exception e) {
    }
    return newConfigs;
  }

  public void applySettings(String tenantId, String json) throws Exception {
    try {
      JsonNode newSettings = new ObjectMapper().readTree(json);
      JsonNode savedConfig = getSavedJson(tenantId);
      List<SystemConfig.SystemConfigMapper> newConfigs = getConfigCopy();

      applySettingsToConfig(savedConfig, newConfigs);
      applySettingsToConfig(newSettings, newConfigs);

      String newConfigJson = getConfigJson(newConfigs);
      saveConfigJson(tenantId, TENANT_CONFIG_KEY, newConfigJson);

    } catch (Exception e) {
      logger.error("Failed apply settings to eixsting config for reason: {}", e);
      throw e;
    }
  }

  public void applySettingsToConfig(JsonNode savedConfig, List<SystemConfig.SystemConfigMapper> configs) throws Exception {
    try {
      configs.stream().forEach(item -> {
        item.getFields().stream().forEach(field -> {
          if (field instanceof SystemConfig.SystemConfigMapper.IntegerField) {
            applyIntegerField(savedConfig, ((SystemConfig.SystemConfigMapper.IntegerField) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.BoolField) {
            applyBoolField(savedConfig, ((SystemConfig.SystemConfigMapper.BoolField) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.StringField) {
            applyStringField(savedConfig, ((SystemConfig.SystemConfigMapper.StringField) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.DecimalField) {
            applyDecimalField(savedConfig, ((SystemConfig.SystemConfigMapper.DecimalField) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.IntegerList) {
            applyIntegerList(savedConfig, ((SystemConfig.SystemConfigMapper.IntegerList) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.OntologyList) {
            applyOntologyList(savedConfig, ((SystemConfig.SystemConfigMapper.OntologyList) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.SearchTypeList) {
            applySearchTypeList(savedConfig, ((SystemConfig.SystemConfigMapper.SearchTypeList) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.SearchTypeWeightList) {
            applySearchTypeWeightList(savedConfig, ((SystemConfig.SystemConfigMapper.SearchTypeWeightList) field));
          }
        });
      });
    } catch (Exception e) {
      logger.error("Failed to build tenant JSON object for reason: {}", e);
    }
  }


  public String getTenantConfigJson(String tenantId) throws Exception {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getTenantConfigJsonObject(tenantId));
  }

  private ObjectNode getTenantConfigJsonObject(String tenantId) throws Exception {
    JsonNode savedConfig = getSavedJson(tenantId);
    return mergeToDefault(savedConfig);
  }

  public ObjectNode mergeToDefault(JsonNode savedConfig) throws Exception {
    try {
      List<SystemConfig.SystemConfigMapper> configs = SystemConfigUtil.getInstance().getConfig();
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode objNode = mapper.createObjectNode();

      configs.stream().forEach(item -> {
        item.getFields().stream().forEach(field -> {
          if (field instanceof SystemConfig.SystemConfigMapper.IntegerField) {
            addIntegerField(savedConfig, objNode, ((SystemConfig.SystemConfigMapper.IntegerField) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.BoolField) {
            addBoolField(savedConfig, objNode, ((SystemConfig.SystemConfigMapper.BoolField) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.StringField) {
            addStringField(savedConfig, objNode, ((SystemConfig.SystemConfigMapper.StringField) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.DecimalField) {
            addDecimalField(savedConfig, objNode, ((SystemConfig.SystemConfigMapper.DecimalField) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.IntegerList) {
            addIntegerList(savedConfig, objNode, ((SystemConfig.SystemConfigMapper.IntegerList) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.OntologyList) {
            addOntologyList(savedConfig, objNode, ((SystemConfig.SystemConfigMapper.OntologyList) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.SearchTypeList) {
            addSearchTypeList(savedConfig, objNode, ((SystemConfig.SystemConfigMapper.SearchTypeList) field));

          } else if (field instanceof SystemConfig.SystemConfigMapper.SearchTypeWeightList) {
            addSearchTypeWeightList(savedConfig, objNode, ((SystemConfig.SystemConfigMapper.SearchTypeWeightList) field));
          }
        });
      });

      return objNode;
    } catch (Exception e) {
      logger.error("Failed to build tenant JSON object for reason: {}", e);
      return null;
    }
  }

  private void addIntegerField(JsonNode savedConfig, ObjectNode objNode, SystemConfig.SystemConfigMapper.IntegerField field) {
    if (savedConfig.get(field.getField()) != null)
      objNode.put(field.getField(), savedConfig.get(field.getField()).intValue());
    else
      objNode.put(field.getField(), field.getDefaultValue());
  }

  private void addBoolField(JsonNode savedConfig, ObjectNode objNode, SystemConfig.SystemConfigMapper.BoolField field) {
    if (savedConfig.get(field.getField()) != null)
      objNode.put(field.getField(), savedConfig.get(field.getField()).booleanValue());
    else
      objNode.put(field.getField(), field.isDefaultValue());
  }

  private void addStringField(JsonNode savedConfig, ObjectNode objNode, SystemConfig.SystemConfigMapper.StringField field) {
    if (savedConfig.get(field.getField()) != null)
      objNode.put(field.getField(), savedConfig.get(field.getField()).textValue());
    else
      objNode.put(field.getField(), field.getDefaultValue());
  }

  private void addDecimalField(JsonNode savedConfig, ObjectNode objNode, SystemConfig.SystemConfigMapper.DecimalField field) {
    if (savedConfig.get(field.getField()) != null)
      objNode.put(field.getField(), savedConfig.get(field.getField()).doubleValue());
    else
      objNode.put(field.getField(), field.getDefaultValue());
  }

  private void addIntegerList(JsonNode savedConfig, ObjectNode objNode, SystemConfig.SystemConfigMapper.IntegerList field) {
    try {
      if (savedConfig.get(field.getField()) != null) {
        List<Integer> savedField = Arrays.asList(mapper.readValue(savedConfig.get(field.getField()).toString(), Integer[].class));
        objNode.putPOJO(field.getField(), savedField);
      } else
        objNode.putPOJO(field.getField(), field.getDefaultValue());
    } catch (Exception e) {
      logger.error("Failed to add IntegerList for reason: {}", e);
    }
  }

  private void addOntologyList(JsonNode savedConfig, ObjectNode objNode, SystemConfig.SystemConfigMapper.OntologyList field) {
    try {
      if (savedConfig.get(field.getField()) != null) {
        List<SystemConfig.SystemConfigMapper.Ontology> savedField = Arrays.asList(mapper.readValue(savedConfig.get(field.getField()).toString(),
          SystemConfig.SystemConfigMapper.Ontology[].class));
        objNode.putPOJO(field.getField(), savedField);
      } else
        objNode.putPOJO(field.getField(), field.getDefaultValue());
    } catch (Exception e) {
      logger.error("Failed to add OntologyList for reason: {}", e);
    }
  }

  private void addSearchTypeList(JsonNode savedConfig, ObjectNode objNode, SystemConfig.SystemConfigMapper.SearchTypeList field) {
    try {
      if (savedConfig.get(field.getField()) != null) {
        List<SystemConfig.SystemConfigMapper.SearchType> savedField = Arrays.asList(mapper.readValue(savedConfig.get(field.getField()).toString(),
          SystemConfig.SystemConfigMapper.SearchType[].class));
        objNode.putPOJO(field.getField(), savedField);
      } else
        objNode.putPOJO(field.getField(), field.getDefaultValue());
    } catch (Exception e) {
      logger.error("Failed to add SearchTypeList for reason: {}", e);
    }
  }

  private void addSearchTypeWeightList(JsonNode savedConfig, ObjectNode objNode, SystemConfig.SystemConfigMapper.SearchTypeWeightList field) {
    try {
      if (savedConfig.get(field.getField()) != null) {
        List<SystemConfig.SystemConfigMapper.SearchTypeWeightDefault> savedField = Arrays.asList(mapper.readValue(savedConfig.get(field.getField()).toString(),
          SystemConfig.SystemConfigMapper.SearchTypeWeightDefault[].class));
        objNode.putPOJO(field.getField(), savedField);
      } else
        objNode.putPOJO(field.getField(), field.getDefaultValue());
    } catch (Exception e) {
      logger.error("Failed to add SearchTypeWeightList for reason: {}", e);
    }
  }

  private void applyIntegerField(JsonNode savedConfig, SystemConfig.SystemConfigMapper.IntegerField field) {
    if (savedConfig.get(field.getField()) != null)
      field.setDefaultValue(savedConfig.get(field.getField()).intValue());
  }

  private void applyBoolField(JsonNode savedConfig, SystemConfig.SystemConfigMapper.BoolField field) {
    if (savedConfig.get(field.getField()) != null)
      field.setDefaultValue(savedConfig.get(field.getField()).booleanValue());
  }

  private void applyStringField(JsonNode savedConfig, SystemConfig.SystemConfigMapper.StringField field) {
    if (savedConfig.get(field.getField()) != null)
      field.setDefaultValue(savedConfig.get(field.getField()).textValue());
  }

  private void applyDecimalField(JsonNode savedConfig, SystemConfig.SystemConfigMapper.DecimalField field) {
    if (savedConfig.get(field.getField()) != null)
      field.setDefaultValue(savedConfig.get(field.getField()).doubleValue());
  }

  private void applyIntegerList(JsonNode savedConfig, SystemConfig.SystemConfigMapper.IntegerList field) {
    try {
      if (savedConfig.get(field.getField()) != null)
        field.setDefaultValue(Arrays.asList(mapper.readValue(savedConfig.get(field.getField()).toString(), Integer[].class)));
    } catch (Exception e) {
      logger.error("Failed to apply IntegerList for reason: {}", e);
    }
  }

  private void applyOntologyList(JsonNode savedConfig, SystemConfig.SystemConfigMapper.OntologyList field) {
    try {
      if (savedConfig.get(field.getField()) != null) {
        List<SystemConfig.SystemConfigMapper.Ontology> savedField = Arrays.asList(mapper.readValue(savedConfig.get(field.getField()).toString(),
          SystemConfig.SystemConfigMapper.Ontology[].class));
        field.setDefaultValue(savedField);
      }
    } catch (Exception e) {
      logger.error("Failed to apply OntologyList for reason: {}", e);
    }
  }

  private void applySearchTypeList(JsonNode savedConfig, SystemConfig.SystemConfigMapper.SearchTypeList field) {
    try {
      if (savedConfig.get(field.getField()) != null) {
        List<SystemConfig.SystemConfigMapper.SearchType> savedField = Arrays.asList(mapper.readValue(savedConfig.get(field.getField()).toString(),
          SystemConfig.SystemConfigMapper.SearchType[].class));
        field.setDefaultValue(savedField);
      }
    } catch (Exception e) {
      logger.error("Failed to apply SearchTypeList for reason: {}", e);
    }
  }

  private void applySearchTypeWeightList(JsonNode savedConfig, SystemConfig.SystemConfigMapper.SearchTypeWeightList field) {
    try {
      if (savedConfig.get(field.getField()) != null) {
        List<SystemConfig.SystemConfigMapper.SearchTypeWeightDefault> savedField = Arrays.asList(mapper.readValue(savedConfig.get(field.getField()).toString(),
          SystemConfig.SystemConfigMapper.SearchTypeWeightDefault[].class));
        field.setDefaultValue(savedField);
      }
    } catch (Exception e) {
      logger.error("Failed to apply SearchTypeWeightList for reason: {}", e);
    }
  }

  public int getKeyAsInteger(String tenantId, String key, int defaultVal) {
    try {
      return getKeyAsInteger(tenantId, key);
    } catch (Exception e) {
      return defaultVal;
    }
  }

  public int getKeyAsInteger(String tenantId, String key) throws Exception {
    AtomicReference<SystemConfig.SystemConfigMapper.IntegerField> ref = new AtomicReference<>();
    try {
      List<SystemConfig.SystemConfigMapper> configs = getTenantConfig(tenantId);
      configs.stream().forEach(config -> {
        config.getFields().forEach(field -> {
          if ((field instanceof SystemConfig.SystemConfigMapper.IntegerField) && field.getField().equals(key))
            ref.set((SystemConfig.SystemConfigMapper.IntegerField) field);
        });
      });
    } catch (Exception e) {
      throw e;
    }
    return ref.get().getDefaultValue();
  }

  public boolean getKeyAsBoolean(String tenantId, String key, boolean defaultVal) {
    try {
      return getKeyAsBoolean(tenantId, key);
    } catch (Exception e) {
      return defaultVal;
    }
  }

  public boolean getKeyAsBoolean(String tenantId, String key) throws Exception {
    AtomicReference<SystemConfig.SystemConfigMapper.BoolField> ref = new AtomicReference<>();
    try {
      List<SystemConfig.SystemConfigMapper> configs = getTenantConfig(tenantId);
      configs.stream().forEach(config -> {
        config.getFields().forEach(field -> {
          if ((field instanceof SystemConfig.SystemConfigMapper.BoolField) && field.getField().equals(key))
            ref.set((SystemConfig.SystemConfigMapper.BoolField) field);
        });
      });
    } catch (Exception e) {
      throw e;
    }
    return ref.get().isDefaultValue();
  }

  public float getKeyAsFloat(String tenantId, String key, float defaultVal) {
    try {
      return getKeyAsFloat(tenantId, key);
    } catch (Exception e) {
      return defaultVal;
    }
  }

  public float getKeyAsFloat(String tenantId, String key) throws Exception {
    AtomicReference<SystemConfig.SystemConfigMapper.DecimalField> ref = new AtomicReference<>();
    try {
      List<SystemConfig.SystemConfigMapper> configs = getTenantConfig(tenantId);
      configs.stream().forEach(config -> {
        config.getFields().forEach(field -> {
          if ((field instanceof SystemConfig.SystemConfigMapper.DecimalField) && field.getField().equals(key))
            ref.set((SystemConfig.SystemConfigMapper.DecimalField) field);
        });
      });
    } catch (Exception e) {
      throw e;
    }
    return (float) ref.get().getDefaultValue();
  }

  public String getKeyAsString(String tenantId, String key, String defaultVal) {
    try {
      return getKeyAsString(tenantId, key);
    } catch (Exception e) {
      return defaultVal;
    }
  }

  public String getKeyAsString(String tenantId, String key) throws Exception {
    AtomicReference<SystemConfig.SystemConfigMapper.StringField> ref = new AtomicReference<>();
    try {
      List<SystemConfig.SystemConfigMapper> configs = getTenantConfig(tenantId);
      configs.stream().forEach(config -> {
        config.getFields().forEach(field -> {
          if ((field instanceof SystemConfig.SystemConfigMapper.StringField) && field.getField().equals(key))
            ref.set((SystemConfig.SystemConfigMapper.StringField) field);
        });
      });
    } catch (Exception e) {
      throw e;
    }
    return ref.get().getDefaultValue();
  }

  public List<SystemConfig.SystemConfigMapper.Ontology> getOntologyFilter(
    String tenantId, List<SystemConfig.SystemConfigMapper.Ontology> defaultVal) {
    try {
      return getOntologyFilter(tenantId);
    } catch (Exception e) {
      return defaultVal;
    }
  }

  public List<SystemConfig.SystemConfigMapper.Ontology> getOntologyFilter(String tenantId) throws Exception {
    String key = "ontologyFilter";
    AtomicReference<SystemConfig.SystemConfigMapper.OntologyList> ref = new AtomicReference<>();
    try {
      List<SystemConfig.SystemConfigMapper> configs = getTenantConfig(tenantId);
      configs.stream().forEach(config -> {
        config.getFields().forEach(field -> {
          if ((field instanceof SystemConfig.SystemConfigMapper.OntologyList) && field.getField().equals(key))
            ref.set((SystemConfig.SystemConfigMapper.OntologyList) field);
        });
      });
    } catch (Exception e) {
      throw e;
    }
    return ref.get().getDefaultValue();
  }

  public List<SystemConfig.SystemConfigMapper.SearchType> getSearchOrder(
    String tenantId, List<SystemConfig.SystemConfigMapper.SearchType> defaultVal) {
    try {
      return getSearchOrder(tenantId);
    } catch (Exception e) {
      return defaultVal;
    }
  }

  public List<SystemConfig.SystemConfigMapper.SearchType> getSearchOrder(String tenantId) throws Exception {
    String key = "defaultSearchOrder";
    AtomicReference<SystemConfig.SystemConfigMapper.SearchTypeList> ref = new AtomicReference<>();
    try {
      List<SystemConfig.SystemConfigMapper> configs = getTenantConfig(tenantId);
      configs.stream().forEach(config -> {
        config.getFields().forEach(field -> {
          if ((field instanceof SystemConfig.SystemConfigMapper.SearchTypeList) && field.getField().equals(key))
            ref.set((SystemConfig.SystemConfigMapper.SearchTypeList) field);
        });
      });
    } catch (Exception e) {
      throw e;
    }
    return ref.get().getDefaultValue();
  }


  public List<Integer> getSpanQuerySLOP(String tenantId, List<Integer> defaultVal) {
    try {
      return getSpanQuerySLOP(tenantId);
    } catch (Exception e) {
      return defaultVal;
    }
  }

  public List<Integer> getSpanQuerySLOP(String tenantId) throws Exception {
    String key = "spanQuerySLOP";
    AtomicReference<SystemConfig.SystemConfigMapper.IntegerList> ref = new AtomicReference<>();
    try {
      List<SystemConfig.SystemConfigMapper> configs = getTenantConfig(tenantId);
      configs.stream().forEach(config -> {
        config.getFields().forEach(field -> {
          if ((field instanceof SystemConfig.SystemConfigMapper.IntegerList) && field.getField().equals(key))
            ref.set((SystemConfig.SystemConfigMapper.IntegerList) field);
        });
      });
    } catch (Exception e) {
      throw e;
    }
    return ref.get().getDefaultValue();
  }


  public List<SystemConfig.SystemConfigMapper.SearchTypeWeightDefault> getSearchTypeWeights(
    String tenantId, List<SystemConfig.SystemConfigMapper.SearchTypeWeightDefault> defaultVal) {
    try {
      return getSearchTypeWeights(tenantId);
    } catch (Exception e) {
      return defaultVal;
    }
  }

  public List<SystemConfig.SystemConfigMapper.SearchTypeWeightDefault> getSearchTypeWeights(String tenantId) throws Exception {
    String key = "searchTypeWeights";
    AtomicReference<SystemConfig.SystemConfigMapper.SearchTypeWeightList> ref = new AtomicReference<>();
    try {
      List<SystemConfig.SystemConfigMapper> configs = getTenantConfig(tenantId);
      configs.stream().forEach(config -> {
        config.getFields().forEach(field -> {
          if ((field instanceof SystemConfig.SystemConfigMapper.SearchTypeWeightList) && field.getField().equals(key))
            ref.set((SystemConfig.SystemConfigMapper.SearchTypeWeightList) field);
        });
      });
    } catch (Exception e) {
      throw e;
    }
    return ref.get().getDefaultValue();
  }

}
