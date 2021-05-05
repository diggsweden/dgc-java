/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a result for the validation of an object against value sets, see {@link ValueSetValidator}.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class ValueSetValidationResult {

  /**
   * Status for validation.
   */
  public enum Status {

    /** Success - checked property/properties are in accordance with value sets. */
    SUCCESS,

    /** Undetermine - missing value set provider - could not check property. */
    UNDETERMINE,

    /** Error - checked property/properties are not in accordance with value sets. */
    ERROR;
  }

  /** The overall result for all checked properties. */
  private Status result;

  /** The property that was checked. */
  private String propertyName;

  /** (Error) message. */
  private String message;

  /** Validation results for the children of the current property. */
  private final List<ValueSetValidationResult> children = new ArrayList<>();

  /**
   * Default constructor.
   */
  public ValueSetValidationResult() {
    this.result = Status.SUCCESS;
  }

  /**
   * Constructor setting up the root property name.
   *
   * @param propertyName
   *          the root property name
   */
  public ValueSetValidationResult(final String propertyName) {
    this();
    this.propertyName = propertyName;
  }

  /**
   * Constructor setting the property name, result and result message
   *
   * @param propertyName
   *          the root property name
   * @param result
   *          the result
   * @param message
   *          the message
   */
  public ValueSetValidationResult(final String propertyName, final Status result, final String message) {
    this(propertyName);
    this.result = result;
    this.message = message;
  }

  /**
   * Predicate that tells if this object represents a successful result.
   *
   * @return true for success and false otherwise
   */
  public boolean isSuccess() {
    return this.result == Status.SUCCESS;
  }

  /**
   * Adds a result for the validation of a child property of the root object.
   *
   * @param childResult
   *          the result
   */
  public void addChildResult(final ValueSetValidationResult childResult) {
    if (childResult == null) {
      return;
    }
    final String childProperty = this.propertyName != null
        ? this.propertyName + "." + childResult.getPropertyName()
        : childResult.getPropertyName();

    if (childResult.getResult() == Status.UNDETERMINE && this.result != Status.ERROR) {
      this.result = Status.UNDETERMINE;
    }
    else if (childResult.getResult() == Status.ERROR) {
      this.result = Status.ERROR;
    }
    final String msg = String.format("%s", childResult.getMessage() != null ? childResult.getMessage() : childResult.getResult());
    if (this.message != null) {
      this.message = String.format("%s, %s", this.message, msg);
    }
    else {
      this.message = msg;
    }
    ValueSetValidationResult vr = new ValueSetValidationResult(childProperty, childResult.getResult(), childResult.getMessage());
    childResult.getChildren().forEach(vr::addChildResult);
    this.children.add(vr);
  }

  /**
   * Gets the overall result.
   *
   * @return the result
   */
  public Status getResult() {
    return this.result;
  }

  /**
   * Gets the property name for the object being validation.
   *
   * @return the property name, or null if this is the root object
   */
  public String getPropertyName() {
    return this.propertyName;
  }

  /**
   * Gets the result message.
   *
   * @return the message
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * Gets the validation results of all childrens to this root object.
   *
   * @return the children or null
   */
  public List<ValueSetValidationResult> getChildren() {
    return this.children;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("ValueSetValidationResult [result=%s, propertyName='%s', message='%s', children=%s]",
      this.result, this.propertyName, this.message, this.children.size());
  }

}
