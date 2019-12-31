package com.dankegongyu.app.common;

import com.google.common.collect.AbstractIterator;
import org.springframework.cglib.beans.BeanMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Iterables {

    public static <T> Iterable<List<T>> chunk(IChunkList iChunk) {
        return new Iterable<List<T>>() {
            @Override
            public Iterator<List<T>> iterator() {
                return new AbstractIterator<List<T>>() {
                    @Override
                    protected List<T> computeNext() {
                        List<T> result = iChunk.fetchList();
                        if (result == null || result.isEmpty()) {
                            return endOfData();
                        } else {
                            return result;
                        }
                    }
                };
            }
        };
    }

    public static <T> Split<T> minid(Object params) {
        return new Split<T>(params) {

            @Override
            public List<T> dealList(List<T> list) {
                if (list != null && !list.isEmpty())
                    params.put("mixid", JsonUtils.convert(list.get(list.size() - 1), Map.class).get("id"));
                return list;
            }
        };
    }

    public static <T> Split<T> page(int pageSize) {

        return new Split<T>(new HashMap() {{
            put("pn", 1);
            put("ps", pageSize);
        }}) {

            @Override
            public List<T> dealList(List<T> list) {
                if (list != null && !list.isEmpty())
                    params.put("pn", Integer.parseInt(params.get("pn").toString()) + 1);
                return list;
            }
        };
    }

    public static <T> Split<T> page(Object params) {
        return new Split<T>(params) {

            @Override
            public List<T> dealList(List<T> list) {
                if (list != null && !list.isEmpty())
                    params.put("pn", Integer.parseInt(params.get("pn").toString()) + 1);
                return list;
            }
        };
    }

    @FunctionalInterface
    public static interface IChunkList<T> {
        List<T> fetchList();
    }

    @FunctionalInterface
    public static interface IChunkFunObject<T> {
        List<T> fetchList(Object object);
    }

    @FunctionalInterface
    public static interface IChunkFunMap<T> {
        List<T> fetchList(Map object);
    }

    public abstract static class Split<T> {
        Map params;
        Object originParams;

        public Split(Object _params) {
            if (_params instanceof Map)
                params = (Map) _params;
            else
                params = BeanMap.create(_params);
            originParams = _params;
        }

        public Iterable<List<T>> chunk(IChunkFunObject iChunkFun) {
            return Iterables.chunk(new IChunkList() {
                @Override
                public List<T> fetchList() {
                    return dealList(iChunkFun.fetchList(originParams));
                }
            });
        }

        public Iterable<List<T>> chunk(IChunkFunMap iChunkFunMap) {
            return Iterables.chunk(new IChunkList() {
                @Override
                public List<T> fetchList() {
                    return dealList(iChunkFunMap.fetchList(params));
                }
            });
        }

        public Map getParams() {
            return params;
        }

        public abstract List<T> dealList(List<T> list);
    }

}
