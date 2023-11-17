import { Column, Entity, ManyToOne, OneToMany } from 'typeorm';
import { commonEntity } from 'src/common/common.entity';
import { Calendar } from 'src/calendar/entities/calendar.entity';
import { EventMember } from 'src/event-member/entities/eventMember.entity';
import { RepeatPolicy } from './repeatPolicy.entity';

@Entity()
export class Event extends commonEntity {
  @ManyToOne(() => Calendar, (calendar) => calendar.events, { nullable: false })
  calendar: Calendar;

  @Column({ type: 'varchar', length: 64 })
  title: string;

  @Column({ type: 'timestamp' })
  startDate: Date;

  @Column({ type: 'timestamp' })
  endDate: Date;

  @Column({ type: 'tinyint', default: 1 })
  isJoinable: boolean;

  @Column({ type: 'varchar', length: 255, nullable: true })
  announcement: string;

  @ManyToOne(() => RepeatPolicy, (repeatPolicy) => repeatPolicy.events)
  repeatPolicy: RepeatPolicy;

  @OneToMany(() => EventMember, (eventMember) => eventMember.event)
  eventMembers: EventMember[];
}
