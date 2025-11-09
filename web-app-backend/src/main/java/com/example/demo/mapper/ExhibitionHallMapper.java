package com.example.demo.mapper;

import com.example.demo.vo.ExhibitionHall;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ExhibitionHallMapper {

    ExhibitionHall findByName(String hallName);

    // (Mapper.xml에서 insert 후 생성된 'id'를 'hall' 객체에 다시 넣어줌)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExhibitionHall hall);

    void update(ExhibitionHall hall);
}