/*
 *
 * Copyright 2021 Force Porquillo (strongforce1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.force.codes.mynotes.core.binding

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding

interface ViewBindingHolder<B : ViewBinding> {

    var binding: B?

    // Only valid between onCreateView and onDestroyView.
    fun requireBinding() = checkNotNull(binding)

    fun requireBindingLet(lambda: (B) -> Unit) {
        binding?.let {
            lambda(it)
        }
    }

    fun requireBindingSelf(lambda: B.() -> Unit) {
        binding?.apply {
            lambda(this)
        }
    }

    fun registerBinding(
        binding: B,
        lifecycleOwner: LifecycleOwner
    ) {
        this.binding = binding
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                this@ViewBindingHolder.binding = null
            }
        })
    }
}