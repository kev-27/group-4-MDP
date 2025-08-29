package com.example.mdp;

import java.util.HashMap;
import java.util.Map;

public class Helper {
  protected static final String ROBOT = "ROBOT";
  protected static final String TARGET = "TARGET";
  protected static final String STATUS = "STATUS";
  protected static final String PLOT = "PLOT";
  protected static final String COMMAND = "COMMAND";
  protected static final String TASK2 = "TASK2";

  protected static Map<String, Integer> resources = new HashMap<String, Integer>() {
    {
      put("o1n", R.drawable.obstacle_1_n);
      put("o1e", R.drawable.obstacle_1_e);
      put("o1s", R.drawable.obstacle_1_s);
      put("o1w", R.drawable.obstacle_1_w);

      put("o2n", R.drawable.obstacle_2_n);
      put("o2e", R.drawable.obstacle_2_e);
      put("o2s", R.drawable.obstacle_2_s);
      put("o2w", R.drawable.obstacle_2_w);

      put("o3n", R.drawable.obstacle_3_n);
      put("o3e", R.drawable.obstacle_3_e);
      put("o3s", R.drawable.obstacle_3_s);
      put("o3w", R.drawable.obstacle_3_w);

      put("o4n", R.drawable.obstacle_4_n);
      put("o4e", R.drawable.obstacle_4_e);
      put("o4s", R.drawable.obstacle_4_s);
      put("o4w", R.drawable.obstacle_4_w);

      put("o5n", R.drawable.obstacle_5_n);
      put("o5e", R.drawable.obstacle_5_e);
      put("o5s", R.drawable.obstacle_5_s);
      put("o5w", R.drawable.obstacle_5_w);

      put("o6n", R.drawable.obstacle_6_n);
      put("o6e", R.drawable.obstacle_6_e);
      put("o6s", R.drawable.obstacle_6_s);
      put("o6w", R.drawable.obstacle_6_w);

      put("o7n", R.drawable.obstacle_7_n);
      put("o7e", R.drawable.obstacle_7_e);
      put("o7s", R.drawable.obstacle_7_s);
      put("o7w", R.drawable.obstacle_7_w);

      put("o8n", R.drawable.obstacle_8_n);
      put("o8e", R.drawable.obstacle_8_e);
      put("o8s", R.drawable.obstacle_8_s);
      put("o8w", R.drawable.obstacle_8_w);

      put("11", R.drawable.number_1);
      put("11n", R.drawable.image_id_11_n);
      put("11e", R.drawable.image_id_11_e);
      put("11s", R.drawable.image_id_11_s);
      put("11w", R.drawable.image_id_11_w);

      put("12", R.drawable.number_2);
      put("12n", R.drawable.image_id_12_n);
      put("12e", R.drawable.image_id_12_e);
      put("12s", R.drawable.image_id_12_s);
      put("12w", R.drawable.image_id_12_w);

      put("13", R.drawable.number_3);
      put("13n", R.drawable.image_id_13_n);
      put("13e", R.drawable.image_id_13_e);
      put("13s", R.drawable.image_id_13_s);
      put("13w", R.drawable.image_id_13_w);

      put("14", R.drawable.number_4);
      put("14n", R.drawable.image_id_14_n);
      put("14e", R.drawable.image_id_14_e);
      put("14s", R.drawable.image_id_14_s);
      put("14w", R.drawable.image_id_14_w);

      put("15", R.drawable.number_5);
      put("15n", R.drawable.image_id_15_n);
      put("15e", R.drawable.image_id_15_e);
      put("15s", R.drawable.image_id_15_s);
      put("15w", R.drawable.image_id_15_w);

      put("16", R.drawable.number_6);
      put("16n", R.drawable.image_id_16_n);
      put("16e", R.drawable.image_id_16_e);
      put("16s", R.drawable.image_id_16_s);
      put("16w", R.drawable.image_id_16_w);

      put("17", R.drawable.number_7);
      put("17n", R.drawable.image_id_17_n);
      put("17e", R.drawable.image_id_17_e);
      put("17s", R.drawable.image_id_17_s);
      put("17w", R.drawable.image_id_17_w);

      put("18", R.drawable.number_8);
      put("18n", R.drawable.image_id_18_n);
      put("18e", R.drawable.image_id_18_e);
      put("18s", R.drawable.image_id_18_s);
      put("18w", R.drawable.image_id_18_w);

      put("19", R.drawable.number_9);
      put("19n", R.drawable.image_id_19_n);
      put("19e", R.drawable.image_id_19_e);
      put("19s", R.drawable.image_id_19_s);
      put("19w", R.drawable.image_id_19_w);

      put("20", R.drawable.alphabet_a);
      put("20n", R.drawable.image_id_20_n);
      put("20e", R.drawable.image_id_20_e);
      put("20s", R.drawable.image_id_20_s);
      put("20w", R.drawable.image_id_20_w);

      put("21", R.drawable.alphabet_b);
      put("21n", R.drawable.image_id_21_n);
      put("21e", R.drawable.image_id_21_e);
      put("21s", R.drawable.image_id_21_s);
      put("21w", R.drawable.image_id_21_w);

      put("22", R.drawable.alphabet_c);
      put("22n", R.drawable.image_id_22_n);
      put("22e", R.drawable.image_id_22_e);
      put("22s", R.drawable.image_id_22_s);
      put("22w", R.drawable.image_id_22_w);

      put("23", R.drawable.alphabet_d);
      put("23n", R.drawable.image_id_23_n);
      put("23e", R.drawable.image_id_23_e);
      put("23s", R.drawable.image_id_23_s);
      put("23w", R.drawable.image_id_23_w);

      put("24", R.drawable.alphabet_e);
      put("24n", R.drawable.image_id_24_n);
      put("24e", R.drawable.image_id_24_e);
      put("24s", R.drawable.image_id_24_s);
      put("24w", R.drawable.image_id_24_w);

      put("25", R.drawable.alphabet_f);
      put("25n", R.drawable.image_id_25_n);
      put("25e", R.drawable.image_id_25_e);
      put("25s", R.drawable.image_id_25_s);
      put("25w", R.drawable.image_id_25_w);

      put("26", R.drawable.alphabet_g);
      put("26n", R.drawable.image_id_26_n);
      put("26e", R.drawable.image_id_26_e);
      put("26s", R.drawable.image_id_26_s);
      put("26w", R.drawable.image_id_26_w);

      put("27", R.drawable.alphabet_h);
      put("27n", R.drawable.image_id_27_n);
      put("27e", R.drawable.image_id_27_e);
      put("27s", R.drawable.image_id_27_s);
      put("27w", R.drawable.image_id_27_w);

      put("28", R.drawable.alphabet_s);
      put("28n", R.drawable.image_id_28_n);
      put("28e", R.drawable.image_id_28_e);
      put("28s", R.drawable.image_id_28_s);
      put("28w", R.drawable.image_id_28_w);

      put("29", R.drawable.alphabet_t);
      put("29n", R.drawable.image_id_29_n);
      put("29e", R.drawable.image_id_29_e);
      put("29s", R.drawable.image_id_29_s);
      put("29w", R.drawable.image_id_29_w);

      put("30", R.drawable.alphabet_u);
      put("30n", R.drawable.image_id_30_n);
      put("30e", R.drawable.image_id_30_e);
      put("30s", R.drawable.image_id_30_s);
      put("30w", R.drawable.image_id_30_w);

      put("31", R.drawable.alphabet_v);
      put("31n", R.drawable.image_id_31_n);
      put("31e", R.drawable.image_id_31_e);
      put("31s", R.drawable.image_id_31_s);
      put("31w", R.drawable.image_id_31_w);

      put("32", R.drawable.alphabet_w);
      put("32n", R.drawable.image_id_32_n);
      put("32e", R.drawable.image_id_32_e);
      put("32s", R.drawable.image_id_32_s);
      put("32w", R.drawable.image_id_32_w);

      put("33", R.drawable.alphabet_x);
      put("33n", R.drawable.image_id_33_n);
      put("33e", R.drawable.image_id_33_e);
      put("33s", R.drawable.image_id_33_s);
      put("33w", R.drawable.image_id_33_w);

      put("34", R.drawable.alphabet_y);
      put("34n", R.drawable.image_id_34_n);
      put("34e", R.drawable.image_id_34_e);
      put("34s", R.drawable.image_id_34_s);
      put("34w", R.drawable.image_id_34_w);

      put("35", R.drawable.alphabet_z);
      put("35n", R.drawable.image_id_35_n);
      put("35e", R.drawable.image_id_35_e);
      put("35s", R.drawable.image_id_35_s);
      put("35w", R.drawable.image_id_35_w);

      put("36", R.drawable.arrow_up);
      put("36n", R.drawable.image_id_36_n);
      put("36e", R.drawable.image_id_36_e);
      put("36w", R.drawable.image_id_36_w);
      put("36s", R.drawable.image_id_36_s);

      put("37", R.drawable.arrow_down);
      put("37n", R.drawable.image_id_37_s);
      put("37e", R.drawable.image_id_37_w);
      put("37w", R.drawable.image_id_37_e);
      put("37s", R.drawable.image_id_37_n);

      put("39", R.drawable.arrow_left);
      put("39n", R.drawable.image_id_39_e);
      put("39e", R.drawable.image_id_39_s);
      put("39w", R.drawable.image_id_39_n);
      put("39s", R.drawable.image_id_39_w);

      put("38", R.drawable.arrow_right);
      put("38n", R.drawable.image_id_38_w);
      put("38e", R.drawable.image_id_38_n);
      put("38s", R.drawable.image_id_38_e);
      put("38w", R.drawable.image_id_38_s);

      put("40", R.drawable.newcircle);
      put("40n", R.drawable.image_id_40_n);
      put("40s", R.drawable.image_id_40_s);
      put("40e", R.drawable.image_id_40_e);
      put("40w", R.drawable.image_id_40_w);

      put("41", R.drawable.bullseye);
      //put("42", R.drawable.yellow_question_mark);
      //put("43", R.drawable.red_question_mark);
    }
  };
}
