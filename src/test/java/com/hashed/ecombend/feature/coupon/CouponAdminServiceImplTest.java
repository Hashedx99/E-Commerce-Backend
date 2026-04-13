package com.hashed.ecombend.feature.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.feature.coupon.dto.CouponRequest;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponAdminServiceImpl Tests")
class CouponAdminServiceImplTest {

	@Mock
	private CouponRepository couponRepository;

	@InjectMocks
	private CouponAdminServiceImpl couponAdminService;

	@Test
	@DisplayName("create: percentage value over 100 throws BusinessException")
	void create_percentageOverHundred_throwsException() {
		CouponRequest request = buildRequest(CouponType.PERCENTAGE, new BigDecimal("120"));

		assertThatThrownBy(() -> couponAdminService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("cannot exceed 100");
	}

	@Test
	@DisplayName("create: negative minimum order amount throws BusinessException")
	void create_negativeMinOrder_throwsException() {
		CouponRequest request = buildRequest(CouponType.FIXED_AMOUNT, new BigDecimal("10"));
		request.setMinOrderAmount(new BigDecimal("-1.00"));

		assertThatThrownBy(() -> couponAdminService.create(request))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("cannot be negative");
	}

	@Test
	@DisplayName("create: valid request uppercases code and saves")
	void create_validRequest_savesCoupon() {
		CouponRequest request = buildRequest(CouponType.PERCENTAGE, new BigDecimal("15"));
		request.setCode("spring15");

		when(couponRepository.findByCodeIgnoreCase("spring15")).thenReturn(Optional.empty());
		when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Coupon created = couponAdminService.create(request);

		assertThat(created.getCode()).isEqualTo("SPRING15");
		assertThat(created.getType()).isEqualTo(CouponType.PERCENTAGE);
		assertThat(created.getValue()).isEqualByComparingTo("15");
	}

	private CouponRequest buildRequest(CouponType type, BigDecimal value) {
		CouponRequest request = new CouponRequest();
		request.setCode("WELCOME");
		request.setType(type);
		request.setValue(value);
		return request;
	}
}

