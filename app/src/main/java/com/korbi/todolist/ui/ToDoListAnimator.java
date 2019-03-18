/*
 * Copyright 2019 Korbinian Moser
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.korbi.todolist.ui;

import android.support.v7.widget.DefaultItemAnimator;


//sets the duration of the add and remove animations of the recyclerView
public class ToDoListAnimator extends DefaultItemAnimator {
    @Override
    public long getRemoveDuration() {
        return 350;
    }

    @Override
    public long getChangeDuration() {
        return 400;
    }

    @Override
    public long getAddDuration() {
        return 500;
    }
}
