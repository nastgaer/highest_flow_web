package highest.flow.taobaolive.taobao.service.impl;

import highest.flow.taobaolive.common.utils.ResourceReader;
import highest.flow.taobaolive.taobao.entity.LiveChannel;
import highest.flow.taobaolive.taobao.entity.LiveColumn;
import highest.flow.taobaolive.taobao.entity.ProductCategory;
import highest.flow.taobaolive.taobao.entity.ProductChannel;
import highest.flow.taobaolive.taobao.service.LiveService;
import highest.flow.taobaolive.taobao.service.ProductSearchService;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("liveServiceImpl")
public class LiveServiceImpl implements LiveService {

    private static List<LiveChannel> channels = new ArrayList<>();

    static {
        try {
            String text = ResourceReader.getLiveColumnList();

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> mapRoot = jsonParser.parseMap(text);
            Map<String, Object> mapData = (Map<String, Object>) mapRoot.get("data");
            List list = (List) mapData.get("liveChannelDatas");

            for (Object obj : list) {
                Map<String, Object> map = (Map<String, Object>) obj;
                int channelId = Integer.parseInt(String.valueOf(map.get("identify")));
                String channelTitle = (String) map.get("title");
                String channelDescInfo = (String) map.get("descInfo");
                List liveColumnDatas = (List) map.get("liveColumnDatas");

                List<LiveColumn> columns = new ArrayList<>();

                for (Object obj2 : liveColumnDatas) {
                    Map<String, Object> mapColumnData = (Map<String, Object>) obj2;
                    int columnId = Integer.parseInt(String.valueOf(mapColumnData.get("identify")));
                    String columnTitle = (String) mapColumnData.get("title");
                    String columnDescInfo = (String) mapColumnData.get("descInfo");

                    LiveColumn liveColumn = new LiveColumn();
                    liveColumn.setId(columnId);
                    liveColumn.setTitle(columnTitle);
                    liveColumn.setDescInfo(columnDescInfo);

                    columns.add(liveColumn);
                }

                LiveChannel channel = new LiveChannel();
                channel.setId(channelId);
                channel.setTitle(channelTitle);
                channel.setDescInfo(channelDescInfo);
                channel.setColumns(columns);

                channels.add(channel);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public List<LiveChannel> getChannels() {
        return channels;
    }

    @Override
    public List<LiveColumn> getColumns(int channelId) {
        for (LiveChannel channel : channels) {
            if (channel.getId() == channelId) {
                return channel.getColumns();
            }
        }
        return null;
    }

    @Override
    public LiveColumn getColumn(int channelId, int columnId) {
        List<LiveColumn> columns = getColumns(channelId);
        if (columns == null) {
            return null;
        }

        for (LiveColumn column : columns) {
            if (column.getId() == columnId) {
                return column;
            }
        }
        return null;
    }
}
