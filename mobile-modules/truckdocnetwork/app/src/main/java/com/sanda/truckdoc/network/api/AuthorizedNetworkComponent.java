package com.sanda.truckdoc.network.api;

import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.UserScope;

import dagger.Subcomponent;

/**
 * Created by k.natallie on 06.11.2016.
 */

@UserScope
@Subcomponent()
public interface AuthorizedNetworkComponent {

    AuthorizedBackend authorizedBackend();

    @Subcomponent.Builder
    interface Builder {
        AuthorizedNetworkComponent create();
    }
}
