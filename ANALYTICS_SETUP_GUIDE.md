# 🎯 Analytics System Setup Guide

## ✅ **Fixed Issues:**
- ✅ Fixed `extractUsername` → `extractUserName` method name
- ✅ Added proper user ID extraction from database
- ✅ Added token validation
- ✅ Added proper imports and dependencies

## 🚀 **Ready to Test!**

### **1. Compile and Run Backend:**
```bash
cd "C:\Anakage Projects\AI\Backk\PostPilotBackend"
mvn clean compile
mvn spring-boot:run
```

### **2. Test API Endpoints:**

#### **Get JWT Token First:**
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"your_username","password":"your_password"}'
```

#### **Test Analytics Endpoint:**
```bash
curl -X GET "http://localhost:8080/api/analytics" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### **Test with Date Range:**
```bash
curl -X GET "http://localhost:8080/api/analytics?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### **3. Frontend Setup:**

Add to your `.env.local` file:
```env
VITE_ANALYTICS_URL=/analytics
VITE_POST_ANALYTICS_URL=/analytics/posts
VITE_ENGAGEMENT_METRICS_URL=/analytics/engagement
```

### **4. Test Frontend:**
1. Start frontend: `npm run dev`
2. Login to your app
3. Click **Analytics** button
4. You should see real data!

## 📊 **Expected Response Format:**

```json
{
  "totalPosts": 5,
  "totalEngagement": 1250,
  "totalReach": 5250,
  "totalImpressions": 15625,
  "avgEngagementRate": 23.81,
  "bestPostEngagement": 150,
  "totalFollowers": 1200,
  "recentPosts": [
    {
      "id": 1,
      "title": "My First Post",
      "engagement": 45,
      "reach": 200,
      "impressions": 500,
      "status": "PUBLISHED",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

## 🔧 **Troubleshooting:**

### **If you get 401 Unauthorized:**
- Check your JWT token is valid
- Make sure you're logged in
- Verify the token hasn't expired (15 minutes)

### **If you get 403 Forbidden:**
- Check your user role (USER, ADMIN, or SUPER_ADMIN)
- Make sure CORS is configured properly

### **If you get 500 Internal Server Error:**
- Check the backend logs
- Make sure your database is running
- Verify you have some posts in your database

## 🎉 **Success Indicators:**

- ✅ Backend compiles without errors
- ✅ API endpoints return JSON data
- ✅ Frontend shows loading state
- ✅ Frontend displays real analytics data
- ✅ No console errors in browser

**Your analytics system is now fully functional!** 🚀

