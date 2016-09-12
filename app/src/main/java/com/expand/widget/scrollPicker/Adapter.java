package com.expand.widget.scrollPicker;

/**
 * Created by jian on 16/9/6.
 */
public interface Adapter {
    int getCount();

    Object getItem(int position);

    String getString(int position);

    boolean isEmpty();
}
