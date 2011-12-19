(define foldr
  (lambda (f e l)
    (if (nil? l)
        e
        (f (head l)
           (foldr f e (tail l))))))

(define sum
  (lambda (l) (foldr + 0 l)))

(define product
  (lambda (l) (foldr * 1 l)))

(define map
  (lambda (f l)
    (if (nil? l)
        (nil)
        (cons (f (head l))
              (map f (tail l))))))

(define square
  (lambda (x) (* x x)))

(define numbers
  (cons 1 (cons 2 (cons 3 (cons 4 (nil))))))
