/*
 * Copyright (c) 2015-2016, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package okapi.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.AsyncMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import okapi.bean.DeploymentDescriptor;
import okapi.util.AsyncMapFactory;
import static okapi.util.ErrorType.*;
import okapi.util.ExtendedAsyncResult;
import okapi.util.Failure;
import okapi.util.Success;

public class DiscoveryManager {

  static class DeploymentList {
    @JsonProperty
    List<DeploymentDescriptor> mdlist = new ArrayList<>();
  }

  AsyncMap<String, String> list = null;

  public void init(Vertx vertx, Handler<ExtendedAsyncResult<Void>> fut) {
    AsyncMapFactory.<String, String>create(vertx, "discoveryList", res -> {
      if (res.succeeded()) {
        this.list = res.result();
        fut.handle(new Success<>());
      } else {
        fut.handle(new Failure<>(INTERNAL, res.cause()));
      }
    });
  }

  void add(DeploymentDescriptor md, Handler<ExtendedAsyncResult<DeploymentDescriptor>> fut) {
    final String srvcId = md.getSrvcId();
    if (srvcId == null) {
      fut.handle(new Failure<>(USER, "Needs srvc"));
      return;

    }
    final String instId = md.getInstId();
    if (instId == null) {
      fut.handle(new Failure<>(USER, "Needs instId"));
      return;
    }
    DeploymentList dpl = new DeploymentList();
    list.get(srvcId, resGet -> {
      if (resGet.failed()) {
        fut.handle(new Failure<>(INTERNAL, resGet.cause()));
      } else {
        String val = resGet.result();
        if (val != null) {
          dpl.mdlist.addAll(Json.decodeValue(val, DeploymentList.class).mdlist);
        }
        Iterator<DeploymentDescriptor> it = dpl.mdlist.iterator();
        while (it.hasNext()) {
          DeploymentDescriptor mdi = it.next();
          if (mdi.getInstId().equals(instId)) {
            fut.handle(new Failure<>(USER, "Duplicate instance " + instId));
            return;
          }
        }
        dpl.mdlist.add(md);
        String newVal = Json.encodePrettily(dpl);
        list.put(srvcId, newVal, resPut -> {
          if (resPut.succeeded()) {
            fut.handle(new Success<>(md));
          } else {
            fut.handle(new Failure<>(INTERNAL, resPut.cause()));
          }
        });
      }
    });
  }

  void remove(String srvcId, String instId, Handler<ExtendedAsyncResult<Void>> fut) {
    list.get(srvcId, resGet -> {
      if (resGet.failed()) {
        fut.handle(new Failure<>(INTERNAL, resGet.cause()));
      } else {
        String val = resGet.result();
        if (val == null) {
          fut.handle(new Failure<>(NOT_FOUND, srvcId));
          return;
        }
        DeploymentList dpl = Json.decodeValue(val, DeploymentList.class);
        Iterator<DeploymentDescriptor> it = dpl.mdlist.iterator();
        while (it.hasNext()) {
          DeploymentDescriptor md = it.next();
          if (md.getInstId().equals(instId)) {
            it.remove();
            if (dpl.mdlist.isEmpty()) {
              list.remove(srvcId, resDel -> {
                if (resDel.succeeded()) {
                  fut.handle(new Success<>());
                } else {
                  fut.handle(new Failure<>(INTERNAL, resDel.cause()));
                }

              });
            } else {
              String newVal = Json.encodePrettily(dpl);
              list.put(srvcId, newVal, resPut -> {
                if (resPut.succeeded()) {
                  fut.handle(new Success<>());
                } else {
                  fut.handle(new Failure<>(INTERNAL, resPut.cause()));
                }
              });
            }
            return;
          }
        }
        fut.handle(new Failure<>(NOT_FOUND, instId));
      }
    });
  }

  void get(String srvcId, String instId, Handler<ExtendedAsyncResult<DeploymentDescriptor>> fut) {
    list.get(srvcId, resGet -> {
      if (resGet.failed()) {
        fut.handle(new Failure<>(NOT_FOUND, srvcId));
      } else {
        String val = resGet.result();
        DeploymentList dpl = Json.decodeValue(val, DeploymentList.class);
        Iterator<DeploymentDescriptor> it = dpl.mdlist.iterator();
        while (it.hasNext()) {
          DeploymentDescriptor md = it.next();
          if (md.getInstId().equals(instId)) {
            fut.handle(new Success<>(md));
            return;
          }
        }
        fut.handle(new Failure<>(NOT_FOUND, instId));
      }
    });
  }

  public void get(String srvcId, Handler<ExtendedAsyncResult<List<DeploymentDescriptor>>> fut) {
    list.get(srvcId, resGet -> {
      if (resGet.failed()) {
        fut.handle(new Failure<>(INTERNAL, resGet.cause()));
      } else {
        String val = resGet.result();
        if (val == null) {
          fut.handle(new Failure<>(NOT_FOUND, srvcId));
        } else {
          DeploymentList dpl = Json.decodeValue(val, DeploymentList.class);
          fut.handle(new Success<>(dpl.mdlist));
        }
      }
    });
  }

  void get(Handler<ExtendedAsyncResult<List<DeploymentDescriptor>>> fut) {
    fut.handle(new Failure<>(INTERNAL, "get not implemented"));
  }
}