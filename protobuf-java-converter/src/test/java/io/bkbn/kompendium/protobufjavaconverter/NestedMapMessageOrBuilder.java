// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/bkbn/kompendium/protobufjavaconverter/converters/test.proto

package io.bkbn.kompendium.protobufjavaconverter;

public interface NestedMapMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:io.bkbn.kompendium.protobufjavaconverter.NestedMapMessage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>map&lt;string, .io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage&gt; map_field = 1;</code>
   */
  int getMapFieldCount();
  /**
   * <code>map&lt;string, .io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage&gt; map_field = 1;</code>
   */
  boolean containsMapField(
      java.lang.String key);
  /**
   * Use {@link #getMapFieldMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage>
  getMapField();
  /**
   * <code>map&lt;string, .io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage&gt; map_field = 1;</code>
   */
  java.util.Map<java.lang.String, io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage>
  getMapFieldMap();
  /**
   * <code>map&lt;string, .io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage&gt; map_field = 1;</code>
   */

  io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage getMapFieldOrDefault(
      java.lang.String key,
      io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage defaultValue);
  /**
   * <code>map&lt;string, .io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage&gt; map_field = 1;</code>
   */

  io.bkbn.kompendium.protobufjavaconverter.SimpleTestMessage getMapFieldOrThrow(
      java.lang.String key);
}