package uz.mediasolutions.taxiservicebot.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import uz.mediasolutions.taxiservicebot.entity.template.AbsLong;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode(callSuper = true)
@Entity(name = "region")
@Where(clause = "deleted=false")
@SQLDelete(sql = "UPDATE region SET deleted=true WHERE id=?")
public class Region extends AbsLong {

    @Column(nullable = false, name = "name")
    private String name;

}
