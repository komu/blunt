(define nil (make-nil))

(define foldr
  (lambda (f e l)
    (if (nil? l)
        e
        (f (head l)
           (foldr f e (tail l))))))

(define sum
  (lambda (l)
    (foldr + 0 l)))

(define product
  (lambda (l)
    (foldr * 1 l)))

(define map
  (lambda (f l)
    (foldr (lambda (x y) (cons (f x) y))
           nil
           l)))

(define filter
  (lambda (p l)
    (foldr (lambda (x y) (if (p x) (cons x y) y))
           nil
           l)))

(define square
  (lambda (x) (* x x)))

(define numbers
  (cons 1 (cons 2 (cons 3 (cons 4 nil)))))
