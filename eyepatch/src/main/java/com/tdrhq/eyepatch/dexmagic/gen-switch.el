(defun gen-invoc-case (i)
  (let ((start (point)))
    (insert "case ")
    (insert (number-to-string i))
    (insert ":\n")
    (insert "return mockDelegate.invoke")
    (insert (number-to-string i))
    (insert "(")
    (loop for j from 0 to (- i 1)
          do
          (insert "args[")
          (insert (number-to-string j))
          (insert "], "))
    (when (> i 0)
      (delete-backward-char 2))
    (insert ");\n")
    (c-indent-line-or-region start (point))))

(defun gen-invoc-case-all ()
  (loop for i from 0 to 20
        do
        (gen-invoc-case i)))
