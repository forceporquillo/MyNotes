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

package com.force.codes.mynotes.core.widget.swipeadater.util

import androidx.recyclerview.widget.DiffUtil

abstract class DragDropSwipeDiffCallback<T>(private val oldList: List<T>, private val newList: List<T>)
    : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            isSameItem(oldList[oldItemPosition], newList[newItemPosition])

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int) =
            isSameContent(oldList[oldPosition], newList[newPosition])

    /**
     * Determines whether or not the items are the same item.
     *
     * @param oldItem The old item.
     * @param newItem The new item.
     * @return True if the items are the same one; false otherwise.
     */
    abstract fun isSameItem(oldItem: T, newItem: T): Boolean

    /**
     * Determines whether or not the items have the same content.
     *
     * @param oldItem The old item.
     * @param newItem The new item.
     * @return True if the items have the same content; false otherwise.
     */
    abstract fun isSameContent(oldItem: T, newItem: T): Boolean
}