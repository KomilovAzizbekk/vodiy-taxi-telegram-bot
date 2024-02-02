package uz.mediasolutions.taxiservicebot.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import uz.mediasolutions.taxiservicebot.entity.template.AbsLong;

import javax.persistence.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode(callSuper = true)
@Entity(name = "district")
@Where(clause = "deleted=false")
@SQLDelete(sql = "UPDATE district SET deleted=true WHERE id=?")
public class District extends AbsLong {

    @Column(nullable = false, name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Region region;

}
