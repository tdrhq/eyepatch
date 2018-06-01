// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

/*

(defun gen-invoke (num)
  (interactive)
  (insert " public Object invoke")
  (insert (number-to-string num))
  (insert "(")
  (loop for i from 0 to (- num 1)
     do
       (insert "Object a")
       (insert (number-to-string i))
       (insert ","))
  (when (> num 0)
     (delete-backward-char 1))
  (insert ");\n"))

(defun gen-invoke-all ()
  (loop for i from 0 to 20
    do
    (gen-invoke i)))
 */
public interface MockDelegate {
 public Object invoke0();
 public Object invoke1(Object a0);
 public Object invoke2(Object a0,Object a1);
 public Object invoke3(Object a0,Object a1,Object a2);
 public Object invoke4(Object a0,Object a1,Object a2,Object a3);
 public Object invoke5(Object a0,Object a1,Object a2,Object a3,Object a4);
 public Object invoke6(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5);
 public Object invoke7(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6);
 public Object invoke8(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7);
 public Object invoke9(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8);
 public Object invoke10(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9);
 public Object invoke11(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10);
 public Object invoke12(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10,Object a11);
 public Object invoke13(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10,Object a11,Object a12);
 public Object invoke14(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10,Object a11,Object a12,Object a13);
 public Object invoke15(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10,Object a11,Object a12,Object a13,Object a14);
 public Object invoke16(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10,Object a11,Object a12,Object a13,Object a14,Object a15);
 public Object invoke17(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10,Object a11,Object a12,Object a13,Object a14,Object a15,Object a16);
 public Object invoke18(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10,Object a11,Object a12,Object a13,Object a14,Object a15,Object a16,Object a17);
 public Object invoke19(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10,Object a11,Object a12,Object a13,Object a14,Object a15,Object a16,Object a17,Object a18);
 public Object invoke20(Object a0,Object a1,Object a2,Object a3,Object a4,Object a5,Object a6,Object a7,Object a8,Object a9,Object a10,Object a11,Object a12,Object a13,Object a14,Object a15,Object a16,Object a17,Object a18,Object a19);

}
