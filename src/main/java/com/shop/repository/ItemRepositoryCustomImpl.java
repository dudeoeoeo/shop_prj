package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import com.shop.entity.QItemImg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

// ItemRepositoryCustom interface 를 구현, Impl 을 붙여야 정상적으로 동작한다.
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{

    // 동적으로 쿼리를 생성하기 위해 JPAQueryFactory 클래스를 사용한다.
    private JPAQueryFactory queryFactory;

    // JPAQueryFactory의 생성자로 EntityManager 객체를 넣어준다.
    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 상품 판매 상태 조건이 전체(null)일 경우는 null을 리턴한다.
    // 결과값이 null 이면 where 절에서 해당 조건은 무시한다.
    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
    }

    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all", searchDateType) || searchDateType == null)
            return null;
        else if(StringUtils.equals("1d", searchDateType))
            dateTime = dateTime.minusDays(1);
        else if(StringUtils.equals("1w", searchDateType))
            dateTime = dateTime.minusWeeks(1);
        else if(StringUtils.equals("1m", searchDateType))
            dateTime = dateTime.minusMonths(1);
        else if(StringUtils.equals("6m", searchDateType))
            dateTime = dateTime.minusMonths(6);

        return QItem.item.regTime.after(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery) {

        if(StringUtils.equals("itemName", searchBy))
            return QItem.item.itemName.like("%" + searchQuery + "%");
        else if(StringUtils.equals("createdBy", searchBy))
            return QItem.item.createdBy.like("%" + searchQuery + "%");

        return null;
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        // selectFrom(QItem.item) => 상품 데이터를 조회하기 위해 QItem의 item을 지정한다.
        QueryResults<Item> results = queryFactory
                .selectFrom(QItem.item)
                // BooleanExpression 을 반환하는 조건문들을 넣어준다. ',' 단위로 넣으면 and 조건으로 인식한다.
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                 searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                 searchByLike(itemSearchDto.getSearchBy(),
        itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                // 데이터를 가지고 올 시작 인덱스 지정
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                // 조회한 리스트 및 전체 개수를 포함하는 QueryResults 를 반환한다.
                // 상품 데이터 리스트 조회 및 상품 데이터 전체 개수를 조회하는 2번의 쿼리문 실행
                .fetchResults();

        List<Item> content = results.getResults();
        long total = results.getTotal();
        
        // 조회한 데이터를 Page 클래스의 구현체인 PageImpl 객체로 반환
        return new PageImpl<>(content, pageable,total);
    }

    private BooleanExpression itemNameLike(String searchQuery) {
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemName.like("%" + searchQuery + "%");
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
                                .select(
                                        new QMainItemDto(
                                        item.id,
                                        item.itemName,
                                        item.itemDetail,
                                        itemImg.imgUrl,
                                        item.price)
                                )
                                .from(itemImg)
                                .join(itemImg.item, item)
                                .where(itemImg.repimgYn.eq("Y"))
                                .where(itemNameLike(itemSearchDto.getSearchQuery()))
                                .orderBy(item.id.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable,total);
    }
}
