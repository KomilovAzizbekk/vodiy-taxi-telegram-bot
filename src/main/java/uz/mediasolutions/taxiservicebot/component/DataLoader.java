package uz.mediasolutions.taxiservicebot.component;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uz.mediasolutions.taxiservicebot.entity.District;
import uz.mediasolutions.taxiservicebot.entity.Region;
import uz.mediasolutions.taxiservicebot.entity.enums.DistrictName;
import uz.mediasolutions.taxiservicebot.entity.enums.RegionName;
import uz.mediasolutions.taxiservicebot.repository.DistrictRepository;
import uz.mediasolutions.taxiservicebot.repository.RegionRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;

    @Value("${spring.sql.init.mode}")
    private String mode;

    @Override
    public void run(String... args) throws Exception {
        if(mode.equals("always")) {
            addRegions();
            addDistrict();
        }
    }


    private void addRegions(){
        Region toshkent = Region.builder().name(RegionName.TOSHKENT.getValue()).build();
        Region andijon = Region.builder().name(RegionName.ANDIJON.getValue()).build();
        regionRepository.saveAll(List.of(toshkent, andijon));
    }

    private void addDistrict(){
        //ANDIJON
        District andijon_t = District.builder().name(DistrictName.ANDIJON_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District andijon_sh = District.builder().name(DistrictName.ANDIJON_SHAHRI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District asaka_sh = District.builder().name(DistrictName.ASAKA_SHAHRI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District asaka_t = District.builder().name(DistrictName.ASAKA_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District baliqchi = District.builder().name(DistrictName.BALIQCHI_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District boz = District.builder().name(DistrictName.BOZ_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District buloqboshi = District.builder().name(DistrictName.BULOQBOSHI_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District xojaobod = District.builder().name(DistrictName.XOJAOBOD_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District xonobod = District.builder().name(DistrictName.XONOBOD_SHAHRI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District jalaquduq = District.builder().name(DistrictName.JALAQUDUQ_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District izboskan = District.builder().name(DistrictName.IZBOSKAN_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District ulugnor = District.builder().name(DistrictName.ULUGNOR_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District marxmat = District.builder().name(DistrictName.MARXAMAT_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District oltinkol = District.builder().name(DistrictName.OLTINKOL_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District paxtaobod = District.builder().name(DistrictName.PAXTAOBOD_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District shahrixon = District.builder().name(DistrictName.SHAHRIXON_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();
        District qorgontepa = District.builder().name(DistrictName.QORGONTEPA_TUMANI.getValue()).
                region(regionRepository.findByName("Andijon")).build();

        //TOSHKENT
        District toshkent = District.builder().name(DistrictName.TOSHKENT_VILOYATI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District bektemir = District.builder().name(DistrictName.BEKTEMIR_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District mirobod = District.builder().name(DistrictName.MIROBOD_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District mirzoUlugbek = District.builder().name(DistrictName.MIRZO_ULUGBEK_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District sergeli = District.builder().name(DistrictName.SERGELI_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District olmazor = District.builder().name(DistrictName.OLMAZOR_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District uchtepa = District.builder().name(DistrictName.UCHTEPA_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District shayxontoxur = District.builder().name(DistrictName.SHAYXONTOXUR_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District yashnobod = District.builder().name(DistrictName.YASHNOBOD_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District chilonzor = District.builder().name(DistrictName.CHILONZOR_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District yunusobod = District.builder().name(DistrictName.YUNUSOBOD_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
        District yakkasaroy = District.builder().name(DistrictName.YAKKASAROY_TUMANI.getValue())
                .region(regionRepository.findByName("Toshkent")).build();
      districtRepository.saveAll(List.of(andijon_sh, andijon_t, asaka_t, asaka_sh, baliqchi, boz,
              buloqboshi, xojaobod, xonobod, jalaquduq, izboskan, ulugnor, marxmat, oltinkol,
              paxtaobod, shahrixon, qorgontepa, toshkent, bektemir, mirobod, mirzoUlugbek, sergeli,
              olmazor, uchtepa, shayxontoxur, yashnobod, chilonzor, yunusobod, yakkasaroy));
    }
}
