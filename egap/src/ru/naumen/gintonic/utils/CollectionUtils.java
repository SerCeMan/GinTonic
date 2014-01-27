package ru.naumen.gintonic.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * Utitlities for collections
 * 
 * @author Sergey Tselovalnikov
 * @since Jan 17, 2014
 */
public final class CollectionUtils {

    public static <E> E getFirst(Collection<E> collection, E defaultValue) {
        Iterator<E> iterator = collection.iterator();
        return iterator.hasNext() ? iterator.next() : defaultValue;
    }
    
    public static <E> E getFirst(Collection<E> collection) {
        return getFirst(collection, null);
    }
    
    
    private CollectionUtils() {
    }
}
