package o.huisang26.mealinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("SetTextI18n")

    InputMethodManager imm;
    EditText editDate;
    TextView status1;

    boolean inDDISH_NM = false, inMMEAL_SC_NM = false;
    String dDISH_NM = null, mMeal_SC_NM = null;
    String code = null;
    boolean inCode = false;
    String date_text = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.enableDefaults();

        status1 = (TextView)findViewById(R.id.textview);
        editDate = (EditText)findViewById(R.id.editDate);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        //에딧텍스트를 눌렀을때 기존에 있던 문자 제거
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDate.setText(null);
            }
        });
        //엔터키에 반응
        editDate.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                switch (keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        date_text = String.valueOf(editDate.getText());
                        HideKeyboard();
                        DiteUpdate();
                        break;
                }
                return false;
            }
        });
        //바탕 눌렀을 때 키보드 숨기기
        status1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDate.setText(null);
                HideKeyboard();
            }
        });


        Date currentTime = Calendar.getInstance().getTime();
        date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        DiteUpdate();
    }

    private void DiteUpdate(){
        if (date_text.equals("")){
            status1.setText("날짜가 입력되지 않았습니다.");
            return;
        }
        status1.setText(date_text.substring(0, 4) + "년 " + date_text.substring(4, 6) + "월 " + date_text.substring(6, 8) + "일\n");
        
        try{
            URL url = new URL("https://open.neis.go.kr/hub/mealServiceDietInfo?KEY=178a8938c5404e889f3f20eee3811ae0&Type=xml&pIndex=1&pSize=100&ATPT_OFCDC_SC_CODE=R10&SD_SCHUL_CODE=8881025" +
                    "&MLSV_YMD=" + date_text); //검색 URL부분

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();

            while (parserEvent != XmlPullParser.END_DOCUMENT){
                switch(parserEvent){
                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if(parser.getName().equals(("CODE"))){
                            inCode = true;
                        }
                        if(parser.getName().equals("MMEAL_SC_NM")){
                            inMMEAL_SC_NM = true;
                        }
                        if(parser.getName().equals("DDISH_NM")){
                            inDDISH_NM = true;
                        }

                        break;

                    case XmlPullParser.TEXT://parser가 내용에 접근했을때
                        if (inCode){
                            code = parser.getText();
                            inCode = false;
                        }
                        if(inDDISH_NM){
                            dDISH_NM = parser.getText();
                            inDDISH_NM = false;
                        }
                        if(inMMEAL_SC_NM){
                            mMeal_SC_NM = parser.getText();
                            inMMEAL_SC_NM = false;
                        }

                        break;

                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("CODE")){
                            if(code.equals("INFO-200")){
                                status1.setText(status1.getText() + "오늘은 급식이 없어요 :)");
                            }
                        }
                        if(parser.getName().equals("MMEAL_SC_NM")){
                            status1.setText(status1.getText() + mMeal_SC_NM + "\n");
                        }
                        if(parser.getName().equals("DDISH_NM")){
                            //원래 문자열을 깔끔하게 정리하기 위한 과정
                            String dish = dDISH_NM.replaceAll("[0-9]", "");
                            dish = dish.replace(".", "");
                            dish = dish.replace("*", "");
                            dish = dish.replace("<br/>", "/");
                            String[] dishs = dish.split("/"); //각 급식 항목들을 dishs[]에 하나씩 넣음

                            for (int i =0; i < dishs.length; i++){
                                status1.setText(status1.getText() + dishs[i] + "\n");
                            }
                            status1.setText(status1.getText() + "\n\n");
                        }

                        break;
                }
                parserEvent = parser.next();
            }
        } catch(Exception e){
            status1.setText("에러가.. 났습니다...\n" + e);
        }
    }

    private void HideKeyboard() {
        imm.hideSoftInputFromWindow(editDate.getWindowToken(), 0);
    }

}