package com.godinsec.privacy.data;

import com.godinsec.privacy.bean.Favorite;
import java.util.Comparator;

/**
 * Created by Seeker on 2016/9/13.
 */

public final class FavoriteComparator implements Comparator<Favorite> {
    @Override
    public int compare(Favorite favorite, Favorite t1) {
        if(favorite == null || t1 == null){
            return 1;
        }
        final int position1 = favorite.getPosition();
        final int position2 = t1.getPosition();
        if(position1 > position2){
            return 1;
        }else if(position1 < position2){
            return -1;
        }
        return 0;
    }
}
