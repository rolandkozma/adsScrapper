package adsScraper.dto;

import java.util.List;

public class MinimumAdsDetailListDto {
	private List<MinimumAdsDetailDto> minimumAdsDetailDtos;

	public MinimumAdsDetailListDto(List<MinimumAdsDetailDto> minimumAdsDetailDtos) {
		this.minimumAdsDetailDtos = minimumAdsDetailDtos;
	}

	public MinimumAdsDetailListDto() {
	}

	public List<MinimumAdsDetailDto> getMinimumAdsDetailDtos() {
		return minimumAdsDetailDtos;
	}

	public void setMinimumAdsDetailDtos(List<MinimumAdsDetailDto> minimumAdsDetailDtos) {
		this.minimumAdsDetailDtos = minimumAdsDetailDtos;
	}

}
