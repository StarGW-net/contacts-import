/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.stargw.contactsimport.authenticator;

import android.accounts.AccountAuthenticatorActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity which displays login screen to the user.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    /** The Intent flag to confirm credentials. */

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {

        Log.i("AuthenticatorActivity", "Function onCreate called. (Do nothing!)");
        super.onCreate(icicle);

    }

}
