import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  UploadedFiles,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { FilesInterceptor } from '@nestjs/platform-express';
import {
  ApiBearerAuth,
  ApiConsumes,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { GetUser } from 'src/auth/get-user.decorator';
import { JwtAuthGuard } from 'src/auth/jwt-auth.guard';
import { multerOptions } from 'src/common/config/multer.config';
import { User } from 'src/user/entities/user.entity';
import { CreateFeedDto } from './dto/create-feed.dto';
import { FeedService } from './feed.service';

@ApiTags('feed')
@Controller('feed')
export class FeedController {
  constructor(private readonly feedService: FeedService) {}

  @UseGuards(JwtAuthGuard)
  @UseInterceptors(FilesInterceptor('contents', 10, multerOptions))
  @Post()
  @ApiOperation({
    summary: '피드 생성 API',
    description: '일정 멤버만 피드를 생성할 수 있습니다.',
  })
  @ApiBearerAuth()
  @ApiConsumes('multipart/form-data')
  createFeed(
    @GetUser() user: User,
    @UploadedFiles() contents: Array<Express.Multer.File>,
    @Body() createFeedDto: CreateFeedDto,
  ) {
    return this.feedService.createFeed(user, contents, createFeedDto);
  }

  @UseGuards(JwtAuthGuard)
  @Get(':id')
  @ApiOperation({
    summary: '피드 조회 API',
    description: '피드를 조회한다.',
  })
  @ApiBearerAuth()
  getFeed(@Param('id') id: number) {
    return this.feedService.getFeed(id);
  }

  @UseGuards(JwtAuthGuard)
  @Delete(':id')
  @ApiOperation({
    summary: '피드 삭제 API',
    description: '일정 주인 또는 피드 작성자는 피드를 삭제할 수 있다.',
  })
  @ApiBearerAuth()
  deleteFeed(@GetUser() user: User, @Param('id') id: number) {
    return this.feedService.deleteFeed(user, id);
  }
}
