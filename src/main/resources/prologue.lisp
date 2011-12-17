(define map
  (lambda (f l)
    (if (nil? l)
        (nil)
        (cons (f (head l))
              (map f (tail l))))))

(define square
  (lambda (x) (* x x)))
