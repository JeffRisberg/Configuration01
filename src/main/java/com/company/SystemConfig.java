package com.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class SystemConfig {
  private static final Logger logger = LoggerFactory.getLogger(SystemConfig.class);
  public static String defaultConfig;

  // static initializer to load default system configuration
  static {
    if (StringUtils.isBlank(defaultConfig)) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        SystemConfig.class.getResourceAsStream("/config/SystemConfiguration.json")))) {

        defaultConfig = reader.lines().collect(Collectors.joining(System.lineSeparator()));

      } catch (Exception e) {
        logger.error("Failed to load default system configuration from resource stream location /config/SystemConfiguration.json");
      }
    }
  }


  public static class SystemConfigMapper {
    @JsonProperty("group")
    private String group;

    @JsonProperty("fields")
    @Getter
    @Setter
    private List<SingleField> fields;


    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    @JsonSubTypes({
      @JsonSubTypes.Type(value = StringField.class, name = "StringField"),
      @JsonSubTypes.Type(value = IntegerField.class, name = "IntegerField"),
      @JsonSubTypes.Type(value = BoolField.class, name = "BoolField"),
      @JsonSubTypes.Type(value = IntegerList.class, name = "IntegerList"),
      @JsonSubTypes.Type(value = DecimalField.class, name = "DecimalField"),
      @JsonSubTypes.Type(value = OntologyList.class, name = "OntologyList"),
      @JsonSubTypes.Type(value = SearchTypeWeightList.class, name = "SearchTypeWeightList"),
      @JsonSubTypes.Type(value = SearchTypeList.class, name = "SearchTypeList")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    public static class SingleField {
      @JsonProperty("@class")
      private String className;

      @JsonProperty("field")
      private String field;

      @JsonProperty("required")
      private boolean required;

      @JsonProperty("label")
      private String label;

      @JsonProperty("type")
      private String type;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class StringField extends SingleField {
      @JsonProperty("defaultValue")
      private String defaultValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class IntegerField extends SingleField {
      @JsonProperty("defaultValue")
      private int defaultValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DecimalField extends SingleField {
      @JsonProperty("defaultValue")
      private double defaultValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static public class IntegerList extends SingleField {
      @JsonProperty("defaultValue")
      private List<Integer> defaultValue;

      @JsonProperty("array")
      private boolean array;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BoolField extends SingleField {
      @JsonProperty("defaultValue")
      private boolean defaultValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OntologyList extends SingleField {
      @JsonProperty("defaultValue")
      private List<Ontology> defaultValue;

      @JsonProperty("array")
      private boolean array;

      @JsonProperty("enums")
      private List<Ontology> enums;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SearchTypeList extends SingleField {
      @JsonProperty("defaultValue")
      private List<SearchType> defaultValue;

      @JsonProperty("array")
      private boolean array;

      @JsonProperty("enums")
      private List<SearchType> enums;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SearchTypeWeightList extends SingleField {

      @JsonProperty("fields")
      private List<SearchTypeWeight> fields;

      @JsonProperty("defaultValue")
      private List<SearchTypeWeightDefault> defaultValue;

      @JsonProperty("array")
      private boolean array;

      @JsonProperty("unique")
      private boolean unique;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    @JsonSubTypes({
      @JsonSubTypes.Type(value = SearchTypeWeightName.class, name = "SearchTypeWeightName"),
      @JsonSubTypes.Type(value = SearchTypeWeightEnum.class, name = "SearchTypeWeightEnum")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    public static class SearchTypeWeight {
      @JsonProperty("field")
      private String field;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SearchTypeWeightName extends SearchTypeWeight {
      @JsonProperty("type")
      private String type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SearchTypeWeightEnum extends SearchTypeWeight {
      @JsonProperty("enums")
      private List<SearchType> enums;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SearchTypeWeightDefault {
      @JsonProperty("searchType")
      private String searchType;

      @JsonProperty("weight")
      private double weight;
    }


    public enum Ontology {
      PERSON,
      NORP,
      FAC,
      ORG,
      GPE,
      LOC,
      PRODUCT,
      EVENT,
      WORK_OF_ART,
      LAW,
      LANGUAGE,
      DATE,
      TIME,
      PERCENT,
      MONEY,
      QUANTITY,
      ORDINAL,
      CARDINAL,
      ITSM,
      HRSM,
      PHONE_NUMBER,
      EMAIL_ADDRESS,
      UNRECOGNIZED
    }

    public enum SearchType {
      PHRASE_IN_SUBJECT,
      BIGRAM_IN_SUBJECT,
      SPAN_IN_SUBJECT_AND_CONTENT,
      SPAN_IN_SUBJECT,
      BIGRAM_IN_HIERARCHICAL_SUBJECT,
      SPAN_IN_HIERARCHICAL_SUBJECT,
      SPAN_IN_CONTENT,
      KEYWORD_IN_TITLE,
      FULLTEXT_KEYWORD,
      FULLTEXT_PHRASE,
      ONTOLOGY,
      ONTOLOGY_IN_SUBJECT,
      ONTOLOGY_IN_CONTENT,
      ENTITY,
      ENTITY_IN_SUBJECT,
      ENTITY_IN_HIERARCHICAL_SUBJECT,
      ENTITY_IN_CONTENT,
      ENTITY_IN_TITLE,
      INTENT_OVERRIDE,
      INCIDENT,
      EXCLUDE_DOCS,
      EXTERNAL_KB_ID,
      KEYWORD_IN_HIERARCHICAL_SUBJECT,
      KEYWORD_IN_CONTENT,
      KEYWORD_IN_SUBJECT,
      SYNONYM
    }

  }
}
