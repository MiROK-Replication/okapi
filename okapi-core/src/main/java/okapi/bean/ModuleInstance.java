/*
 * Copyright (c) 2015-2016, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package okapi.bean;

public class ModuleInstance {

  ModuleDescriptor md;
  ProcessModuleHandle pmh;
  String url;
  int port;
  final RoutingEntry re;

  public ModuleInstance(ModuleDescriptor md, ProcessModuleHandle pmh, String url, int port) {
    this.md = md;
    this.pmh = pmh;
    this.url = url;
    this.port = port;
    this.re = null;
  }

  public ModuleInstance(ModuleInstance mi, RoutingEntry re) {
    this.md = mi.md;
    this.pmh = mi.pmh;
    this.url = mi.url;
    this.port = mi.port;
    this.re = re;
  }

  public ModuleDescriptor getModuleDescriptor() {
    return md;
  }

  public ProcessModuleHandle getProcessModuleHandle() {
    return pmh;
  }

  public String getUrl() {
    return url;
  }

  public RoutingEntry getRoutingEntry() {
    return re;
  }
  public int getPort() {
    return port;
  }
}
