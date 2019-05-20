// NewsArrayAdapter.java
// An ArrayAdapter for displaying a List<News>'s elements in a ListView
package com.example.newsreader;

import android.content.Context;
import android.support.v4.text.HtmlCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class NewsArrayAdapter extends ArrayAdapter<News> {
   // class for reusing views as list items scroll off and onto the screen
   private static class ViewHolder {
      TextView articleTitle;
   }
   public static final String TAG = NewsArrayAdapter.class.getSimpleName();

   // stores already downloaded Bitmaps for reuse
//   private Map<String, Bitmap> bitmaps = new HashMap<>();

   // constructor to initialize superclass inherited members
   public NewsArrayAdapter(Context context, List<News> forecast) {
       super(context, -1, forecast);
   }

   // creates the custom views for the ListView's items
   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      // get News object for this specified ListView position
      News news_obj = getItem(position);
      Log.d(TAG, "getView()");

      ViewHolder viewHolder; // object that reference's list item's views

      // check for reusable ViewHolder from a ListView item that scrolled
      // offscreen; otherwise, create a new ViewHolder
      if (convertView == null) { // no reusable ViewHolder, so create one
         viewHolder = new ViewHolder();
         LayoutInflater inflater = LayoutInflater.from(getContext());
         convertView =
            inflater.inflate(R.layout.list_item, parent, false);
         viewHolder.articleTitle =
            (TextView) convertView.findViewById(R.id.articleTitle);
         convertView.setTag(viewHolder);

      }
      else { // reuse existing ViewHolder stored as the list item's tag
         viewHolder = (ViewHolder) convertView.getTag();
      }

      // get other data from News object and place into views
      Context context = getContext(); // for loading String resources
       String title = context.getString(R.string.article_link, news_obj.newsUrl, news_obj.articleTitle);
      viewHolder.articleTitle.setText(HtmlCompat.fromHtml(title, Html.FROM_HTML_SEPARATOR_LINE_BREAK_DIV));
      viewHolder.articleTitle.setMovementMethod(LinkMovementMethod.getInstance());
      Log.d(TAG, "getView()" + title);

      return convertView; // return completed list item to display
   }
}

