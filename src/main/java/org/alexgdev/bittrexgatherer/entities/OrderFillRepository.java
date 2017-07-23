package org.alexgdev.bittrexgatherer.entities;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderFillRepository extends CrudRepository<OrderFill, Long>{
	Page<OrderFill> findAll(Pageable pageable);


}
